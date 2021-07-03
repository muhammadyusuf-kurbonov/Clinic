package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.LoginScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.MainScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.PermissionScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.ServiceTestScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.allAppPermissions
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

class MainActivity : AppCompatActivity() {

    @ExperimentalPermissionsApi
    @Composable
    fun MainActivityCompose(
        navController: NavHostController,
        appStatesController: AppStatesController
    ) {

        CompositionLocalProvider(
            LocalNavigation provides navController,
            LocalAppControllerProvider provides appStatesController
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

                    // TODO: Remove, it's for test
                    composable("service_test") { ServiceTestScreen() }
                }
                val authState = AppStateStore.authState.collectAsState()
                val tokenIsEmpty = provider.readPreference("token", "").isEmpty()

                LaunchedEffect(key1 = Unit) {
                    val route = intent.extras?.getString("route", null)
                    if (route != null)
                        navController.navigate(route)
                }

                if ((authState.value is AuthState.AuthRequired) or tokenIsEmpty) {
                    LaunchedEffect(key1 = "started") {
                        navController.navigate("login")
                    }
                }
            }
        }
    }

    private lateinit var appStatesController: AppStatesController
    private lateinit var provider: SystemFunctionsProvider
    private lateinit var navController: NavHostController

    @ExperimentalPermissionsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            provider = SystemFunctionsProvider()
            appStatesController = AppStatesController(
                lifecycleScope.coroutineContext,
                provider,
                AppRepository(provider.readPreference("token", ""))
            )
            if (provider.readPreference("token", "").isNotEmpty())
                AppStateStore.updateAuthState(AuthState.AuthSuccess)
            MainActivityCompose(navController = navController, appStatesController)
        }
    }

    override fun onBackPressed() {
        if (navController.currentBackStackEntry?.destination?.route != "main") {
            navController.navigate("main") {
                popUpTo(0)
            }
        } else
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnLogout -> {
                appStatesController.handle(Action.Logout)
            }
//            R.id.mnSettings -> {
//            }
            R.id.mnPermissions -> {
                navController.navigate("permissions")
            }
        }
        return super.onOptionsItemSelected(item)
    }
}