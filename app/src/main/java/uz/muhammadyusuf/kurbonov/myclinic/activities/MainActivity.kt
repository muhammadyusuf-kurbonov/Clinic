package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber


class MainActivity : AppCompatActivity() {

    private val permissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val notGrantedPermissions = mutableListOf<String>()

            it.keys.filter { permission ->
                permission !in arrayListOf(
                    "android.permission.READ_PRIVILEGED_PHONE_STATE"
                )
            }.forEach { permission ->
                if (it[permission] != true)
                    notGrantedPermissions.add(permission)
            }

            if (notGrantedPermissions.isNotEmpty())
                mainTextView.text =
                    getString(R.string.not_granted, notGrantedPermissions.joinToString())
            else
                mainTextView.text = getString(
                    R.string.main_label_text,
                    App.pref.getString("user.email", "(login again to see it)")
                )
        }

    private val overlayRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    mainTextView.setText(R.string.main_label_ask_permission)
                }
            }
        }


    private val mainTextView: TextView by lazy {
        findViewById(R.id.tvMain)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTimber()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        permissionsRequest.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    Manifest.permission.FOREGROUND_SERVICE
                else "",
            )
        )

        verifyToken()

        findViewById<TextView>(R.id.tvVersion).text = getString(
            R.string.version_template,
            getString(R.string.app_name),
            BuildConfig.VERSION_NAME
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // ask for setting
                overlayRequest.launch(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    }

    private fun verifyToken() {
        val token = DI.getToken()

        if (token.trim().isEmpty() or token.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
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
            App.NOTIFICATION_CHANNEL_ID,
            "Notifications of app 32Desk.com",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.enableVibration(true)
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)
    }
}