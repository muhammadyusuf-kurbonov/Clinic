package uz.muhammadyusuf.kurbonov.myclinic

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.main.MainScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme.AppTheme

@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val testRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainScreenAppName() {
        testRule.setContent {
            AppTheme {
                MainScreen(permissionsGranted = true)
            }
        }

        testRule.onNodeWithText(testRule.activity.getString(R.string.app_name))
            .assertIsDisplayed()
    }


    @Test
    fun testMainPermissionPopup() {
        testRule.setContent {
            AppTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavigation provides navController) {
                    MainScreen(permissionsGranted = false)
                }
            }
        }

        testRule
            .onNodeWithText(
                testRule.activity
                    .getString(R.string.main_label_ask_permission)
            )
            .assertIsDisplayed()
    }

    @Test
    fun testNoMainPermissionPopup() {
        testRule.setContent {
            AppTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavigation provides navController) {
                    MainScreen(permissionsGranted = true)
                }
            }
        }

        testRule
            .onNodeWithText(
                testRule.activity
                    .getString(R.string.main_label_ask_permission)
            )
            .assertDoesNotExist()
    }

}