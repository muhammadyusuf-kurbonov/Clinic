package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.api.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityAuthBinding
import uz.muhammadyusuf.kurbonov.myclinic.di.API
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber
import java.net.InetAddress
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHONE = "uz.muhammadyusuf.kurbonov.myclinic.phone"
    }

    private lateinit var binding: ActivityAuthBinding

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTimber()

        binding.btnLogin
            .setOnClickListener {
                setStatus(AuthResult.STARTED)

                val authService = API.getAPIService()
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
                        App.pref.edit()
                            .putString("token", response.body()?.accessToken)
                            .putString("user.email", binding.inputEmail.text.toString())
                            .apply()
                        setStatus(AuthResult.SUCCESS)
                    } else {
                        App.pref.edit()
                            .putString("token", "")
                            .putString("user.email", null)
                            .apply()
                        setStatus(AuthResult.FAILED)
                    }

                    if (response.code() == 407)
                        setStatus(AuthResult.NO_CONNECTION)

                    if (response.isSuccessful) {
                        if (intent.extras?.containsKey(EXTRA_PHONE) == true) {
                            App.getAppViewModelInstance().reduce(
                                Action.Search(
                                    intent.extras!!.getString(
                                        "uz.muhammadyusuf.kurbonov.myclinic.phone",
                                        ""
                                    ), App.getAppViewModelInstance().callDirection
                                )
                            )
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
                    e.printStackTrace()
                    it.resume(false)
                    Timber.e(e)
                }
            }
        }
}
