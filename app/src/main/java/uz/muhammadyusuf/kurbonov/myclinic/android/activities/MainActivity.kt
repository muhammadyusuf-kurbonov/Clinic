package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.LoginScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.MainScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.PermissionScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.AppViewModelProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.allAppPermissions
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme


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

        @ExperimentalPermissionsApi
        @Composable
        fun MainActivityCompose(navController: NavHostController) {

            val permissionsGranted =
                rememberMultiplePermissionsState(permissions = allAppPermissions.toList()).allPermissionsGranted
            CompositionLocalProvider(LocalNavigation provides navController) {
                AppTheme {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") { MainScreen(permissionsGranted) }
                        composable("permissions") { PermissionScreen() }
                    }
                }
            }
        }
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

    private lateinit var navController: NavHostController

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            MainActivityCompose(navController = navController)
        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel()
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                // ask for setting
//                overlayRequest.launch(
//                    Intent(
//                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:$packageName")
//                    )
//                )
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnLogout -> {
            }
            R.id.mnSettings -> {
            }
            R.id.mnPermissions -> {
                navController.navigate("permissions")
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

}