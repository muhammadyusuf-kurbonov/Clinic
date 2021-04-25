package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTimber()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    "android.permission.READ_PRIVILEGED_PHONE_STATE",
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        Manifest.permission.FOREGROUND_SERVICE
                    else "",
                ), 241
            )
        } else {
            findViewById<TextView>(R.id.tvMain).text = getString(
                R.string.main_label_text,
                App.pref.getString("user.email", "(login again to see it)")
            )
        }

        val token = DI.getToken()

        if (token.trim().isEmpty() or token.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<TextView>(R.id.tvVersion).text = getString(
            R.string.version_template,
            getString(R.string.app_name),
            BuildConfig.VERSION_NAME
        )
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
            findViewById<TextView>(R.id.tvMain).text = getString(
                R.string.main_label_text,
                App.pref.getString("user.email", "(login again to see it)")
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnLogout -> {
                App.pref.edit()
                    .putString("token", "")
                    .putString("user.email", "")
                    .apply()

                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "32desk_notification_channel",
            "Notifications of app 32Desk.com",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.tvMain).text = getString(
            R.string.main_label_text,
            App.pref.getString("user.email", "(login again to see it)")
        )
    }
}