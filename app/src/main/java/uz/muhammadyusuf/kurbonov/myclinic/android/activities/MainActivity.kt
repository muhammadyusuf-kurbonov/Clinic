package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.paperdb.Paper
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.LoginScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.MainScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.PermissionScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.AppViewModelProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.allAppPermissions
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository


class MainActivity : AppCompatActivity(), SystemFunctionsProvider {

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

    @ExperimentalPermissionsApi
    @Composable
    fun MainActivityCompose(
        navController: NavHostController,
        appViewModel: AppViewModel
    ) {

        CompositionLocalProvider(
            LocalNavigation provides navController,
            AppViewModelProvider provides appViewModel
        ) {
            AppTheme {
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        val permissionsGranted =
                            rememberMultiplePermissionsState(allAppPermissions.toList())
                                .allPermissionsGranted and
                                    Settings.canDrawOverlays(LocalContext.current)

                        MainScreen(permissionsGranted)
                    }
                    composable("permissions") { PermissionScreen() }
                    composable("login") { LoginScreen() }
                }
                val authState = AppViewModelProvider.current.authState.collectAsState()
                val tokenIsEmpty = readPreference("token", "").isEmpty()

                if ((authState.value is AuthState.AuthRequired) or tokenIsEmpty) {
                    LaunchedEffect(key1 = "started") {
                        navController.navigate("login")
                    }
                }
            }
        }
    }

    private lateinit var appViewModel: AppViewModel
    private lateinit var navController: NavHostController

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            appViewModel = AppViewModel(
                lifecycleScope.coroutineContext,
                this,
                AppRepository(readPreference("token", ""))
            )
            MainActivityCompose(navController = navController, appViewModel)
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
                appViewModel.handle(Action.Logout)
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

    override fun onError(throwable: Throwable): Boolean {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        return true
    }

    override fun writePreference(key: String, value: Any) {
        Paper.book().write(key, value)
    }

    override fun <T> readPreference(key: String, defaultValue: T?): T =
        Paper.book().read(key, defaultValue)
            ?: throw IllegalArgumentException("Default value can\'t be null")


}