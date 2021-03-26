package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityAuthBinding
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder.type
import uz.muhammadyusuf.kurbonov.myclinic.works.NotifyWork
import uz.muhammadyusuf.kurbonov.myclinic.works.SearchWork
import uz.muhammadyusuf.kurbonov.myclinic.works.StartRecognizeWork
import java.net.InetAddress
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        binding.btnLogin
            .setOnClickListener {
                setStatus(AuthResult.STARTED)

                val authService = get<APIService>()
                lifecycleScope.launch {

                    if (!checkInternetConnection()) {
                        setStatus(AuthResult.NO_CONNECTION)
                        return@launch
                    }

                    val response = authService.authenticate(
                        AuthRequest(
                            email = binding.inputEmail.text.toString(),
                            password = binding.inputPassword.text.toString()
                        )
                    )

                    Timber.d("$response")

                    if (response.isSuccessful) {
                        get<SharedPreferences>().edit()
                            .putString("token", response.body()?.accessToken)
                            .apply()
                        setStatus(AuthResult.SUCCESS)
                    } else {
                        setStatus(AuthResult.FAILED)
                    }

                    if (response.code() == 407)
                        setStatus(AuthResult.NO_CONNECTION)

                    if (response.isSuccessful) {
                        if (intent.extras?.containsKey("uz.muhammadyusuf.kurbonov.myclinic.phone") == true) {
                            val enterWorker = OneTimeWorkRequestBuilder<StartRecognizeWork>()

                            enterWorker.setInputData(Data.Builder().apply {
                                putString(StartRecognizeWork.INPUT_PHONE, DataHolder.phoneNumber)
                                putString(StartRecognizeWork.INPUT_TYPE, type?.getAsString())
                            }.build())

                            WorkManager.getInstance(this@LoginActivity).beginWith(
                                enterWorker.build()
                            ).then(OneTimeWorkRequest.from(SearchWork::class.java))
                                .then(OneTimeWorkRequest.from(NotifyWork::class.java))
                                .enqueue()
                        }
                        delay(1500)
                        finish()
                    }
                }
            }
    }

    private fun setStatus(authResult: AuthResult) {
        with(binding) {
            llStatus.visibility = VISIBLE
            when (authResult) {
                AuthResult.STARTED -> {
                    imgStatus.setImageResource(R.drawable.ic_baseline_access_time_24)
                    tvStatus.text = getString(R.string.logging_in_caption)
                    btnLogin.isEnabled = false
                }
                AuthResult.SUCCESS -> {
                    imgStatus.setImageResource(R.drawable.ic_baseline_check_circle_24)
                    tvStatus.text = getString(R.string.login_success)
                }
                AuthResult.FAILED -> {
                    imgStatus.setImageResource(R.drawable.ic_baseline_cancel_24)
                    tvStatus.text = getString(R.string.login_failed)
                    btnLogin.isEnabled = true
                }
                AuthResult.NO_CONNECTION -> {
                    imgStatus.setImageResource(R.drawable.ic_baseline_cancel_24)
                    tvStatus.text = getString(R.string.no_connection)
                    btnLogin.isEnabled = true
                }
            }
        }
    }

    private enum class AuthResult {
        STARTED, SUCCESS, FAILED, NO_CONNECTION
    }


    private suspend fun checkInternetConnection() =
        withContext(Dispatchers.IO) {
            suspendCoroutine<Boolean> {
                try {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    val ipAddress = InetAddress.getByName("google.com")
                    //You can replace it with your name
                    it.resume(!ipAddress.equals(""))
                } catch (e: Exception) {
                    it.resume(false)
                    Timber.e(e)
                }
            }
        }
}
