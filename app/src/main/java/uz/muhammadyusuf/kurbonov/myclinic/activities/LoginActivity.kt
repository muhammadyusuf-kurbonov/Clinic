package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
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
    @SuppressLint("InflateParams")
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

                    val view = LayoutInflater.from(this@LoginActivity)
                        .inflate(R.layout.auth_result, null, false)
                    view.findViewById<ImageView>(R.id.imgStatus).setImageResource(
                        if (response.isSuccessful) R.drawable.ic_baseline_check_circle_24
                        else R.drawable.ic_baseline_cancel_24
                    )
                    view.findViewById<TextView>(R.id.tvStatus).text =
                        if (response.isSuccessful) "Login success"
                        else "Error occurred"

                    Snackbar.make(
                        findViewById(R.id.container),
                        "Template",
                        Snackbar.LENGTH_LONG
                    ).apply {
                        val snackbarLayout = this.view as Snackbar.SnackbarLayout
                        snackbarLayout.removeAllViews()
                        snackbarLayout.addView(view, MATCH_PARENT, MATCH_PARENT)
                    }
                        .show()

                    if (response.isSuccessful) {
                        delay(1500)
                        finish()
                    }
                }
            }
    }
}
