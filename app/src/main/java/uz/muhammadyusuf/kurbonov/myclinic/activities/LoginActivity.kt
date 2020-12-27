package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.context.loadKoinModules
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.di.authModule
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.AppEvent
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.EventBus
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthService

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        loadKoinModules(authModule)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        findViewById<AppCompatButton>(R.id.btn_login)
            .setOnClickListener {
                val authService = get<AuthService>()
                lifecycleScope.launch {
                    val response = authService.authenticate(
                        AuthRequest(
                            email = findViewById<EditText>(R.id.input_email).text.toString(),
                            password = findViewById<EditText>(R.id.input_password).text.toString()
                        )
                    )

                    AlertDialog.Builder(this@LoginActivity)
                        .setIcon(
                            if (response.isSuccessful) R.drawable.ic_baseline_check_circle_24
                            else R.drawable.ic_baseline_cancel_24
                        )
                        .setMessage(
                            if (response.isSuccessful) "Login success"
                            else "Error occurred"
                        )
                        .setNeutralButton(android.R.string.ok) { dialog, _ ->
                            if (response.isSuccessful) {
                                get<SharedPreferences>().edit()
                                    .putString("token", response.body()?.accessToken)
                                    .apply()
                                EventBus.event.value = AppEvent.AuthSucceedEvent
                                Timber.d("New token is ${response.body()?.accessToken}")
                            } else {
                                Timber.d("Error is $response")
                                EventBus.event.value = AppEvent.AuthFailedEvent
                            }
                            dialog.dismiss()
                            finish()
                        }
                        .show()
                }
            }
    }
}
