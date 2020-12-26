package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.REORDER_TASKS,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        Manifest.permission.FOREGROUND_SERVICE
                    else "",
                    "android.permission.READ_PRIVILEGED_PHONE_STATE"
                ), 241
            )
        } else {
            findViewById<TextView>(R.id.tvMain).text = "Ready!"
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
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }) {
            findViewById<TextView>(R.id.tvMain).text = "Ready!"
        }
    }

}