package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.works.BackgroundCheckWorker
import uz.muhammadyusuf.kurbonov.myclinic.android.works.BackgroundCheckWorker.Companion.AUTO_START_PREF_KEY
import uz.muhammadyusuf.kurbonov.myclinic.di.API
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    companion object {
        @Suppress("SpellCheckingInspection")
        private val POWER_MANAGER_INTENTS = arrayOf(
            //region HAWEI
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                )
            ),
            //endregion

            //region XIAOMI
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerSettings"
                )
            ),
            //endregion

            //region LETV
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            //endregion


            //region COLOROS
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            //endregion


            //region OPPO
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            //endregion


            //region IQOO
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            //endregion


            //region VIVO
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            //endregion


            //region SAMSUNG
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            ),
            //endregion

            //region HTC
            Intent().setComponent(
                ComponentName(
                    "com.htc.pitroad",
                    "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                )
            ),
            //endregion


            //region ASUS
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.MainActivity"
                )
            ),
            //endregion

        )

    }

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
            mutableListOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
            ).apply {
                if (Build.MANUFACTURER.contains("huawei", false))
                    add("com.huawei.permission.external_app_settings.USE_COMPONENT")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    add(Manifest.permission.FOREGROUND_SERVICE)
            }.toTypedArray()
        )

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

        requestUnrestrictedBackgroundService()
    }

    private fun verifyToken() {
        val token = API.getToken()

        if (token.trim().isEmpty() or token.isBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            mainTextView.text = getString(
                R.string.main_label_text,
                App.pref.getString("user.email", "(login again to see it)")
            )
        }

    }

    override fun onResume() {
        super.onResume()
        verifyToken()
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
            R.id.mnSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val lowChannel = NotificationChannel(
            App.NOTIFICATION_CHANNEL_ID,
            "Low notifications of app 32Desk.com",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        lowChannel.enableVibration(false)
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(lowChannel)

        val channel = NotificationChannel(
            App.HEADUP_NOTIFICATION_CHANNEL_ID,
            "Notifications of app 32Desk.com",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)
    }

    private fun requestUnrestrictedBackgroundService(): Boolean {
        WorkManager.getInstance(this).enqueue(
            PeriodicWorkRequestBuilder<BackgroundCheckWorker>(25, TimeUnit.MINUTES)
                .build()
        )

        var success = true
        val pref = App.pref
        val lastUpdate = pref.getLong(AUTO_START_PREF_KEY, -1)
        val updated = if (lastUpdate == -1L) {
            val editor = pref.edit()
            editor.putLong(AUTO_START_PREF_KEY, System.currentTimeMillis())
            editor.apply()
            false
        } else lastUpdate != -1L &&
                TimeUnit.MILLISECONDS.toMinutes(
                    System.currentTimeMillis() - lastUpdate
                ) <= 30
        if (!updated) {
            for (intent in POWER_MANAGER_INTENTS)
                if (packageManager.resolveActivity(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    ) != null
                ) {
                    runBlocking {
                        success = success && suspendCoroutine { cont ->
                            val dialog = AlertDialog.Builder(this@MainActivity)
                            val powerManagement =
                                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                                    if (it.resultCode == RESULT_OK) {
                                        val editor = pref.edit()
                                        editor.putLong(
                                            AUTO_START_PREF_KEY,
                                            System.currentTimeMillis()
                                        )
                                        cont.resume(true)
                                        editor.apply()
                                    } else {
                                        cont.resume(false)
                                    }
                                }
                            dialog.setMessage(getString(R.string.ask_background_permission))
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    try {
                                        powerManagement.launch(intent)
                                    } catch (e: SecurityException) {
                                        Timber.d("Battery optimization is forbidden in ${Build.MANUFACTURER} + ${Build.BRAND} + ${Build.MODEL}")
                                        FirebaseCrashlytics.getInstance().recordException(e)
                                        AlertDialog.Builder(this@MainActivity)
                                            .setMessage(getString(R.string.no_power_mangement_access))
                                            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                                dialog.dismiss()
                                                cont.resume(true)
                                            }.show()
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel) { _, _ ->
                                    cont.resume(false)
                                    finish()
                                }
                            dialog.show()
                        }
                    }
                }
        }
        return success
    }
}