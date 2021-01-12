package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationManagerCompat
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    "android.permission.READ_PRIVILEGED_PHONE_STATE",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        Manifest.permission.FOREGROUND_SERVICE
                    else "",
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 241
            )
        } else {
            findViewById<TextView>(R.id.tvMain).text = getString(R.string.ready)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "clinic_info",
                "Notifications for call identification",
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    enableVibration(false)
                }
            NotificationManagerCompat.from(this)
                .createNotificationChannel(channel)
        }

        Timber.d("TimeZone is ${TimeZone.getDefault()}")

        val token = get<String>(named("token"))

        if (token.trim().isEmpty() or token.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<AppCompatButton>(R.id.btn_logout)
            .setOnClickListener {
                get<SharedPreferences>().edit()
                    .putString("token", "")
                    .apply()
                startActivity(Intent(this, LoginActivity::class.java))
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allGiven = true

        grantResults.forEachIndexed { index, i ->
            if (permissions[index] != "android.permission.READ_PRIVILEGED_PHONE_STATE")
                allGiven = allGiven && i == PERMISSION_GRANTED
        }

        if (allGiven) {
            findViewById<TextView>(R.id.tvMain).text = getString(R.string.ready)
        }
    }

}