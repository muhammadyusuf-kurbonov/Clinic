package uz.muhammadyusuf.kurbonov.myclinic

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.LoginScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

@RunWith(AndroidJUnit4::class)
class LoginTest {
    @get: Rule
    val testRule = createAndroidComposeRule<MainActivity>()

    private val scope = CoroutineScope(Dispatchers.Main)

    @Test
    fun testLoginButton() {
        AppStateStore.updateAuthState(AuthState.AuthRequired)
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                LoginScreen()
            }
        }

        testRule.onNodeWithText(testRule.activity.getString(R.string.login)).assertIsDisplayed()
    }

    @Test
    fun testEmailField() {
        AppStateStore.updateAuthState(AuthState.AuthRequired)
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                LoginScreen()
            }
        }

        testRule.onNodeWithText(testRule.activity.getString(R.string.email_hint))
            .assertIsDisplayed()
    }

    @Test
    fun testPasswordField() {
        AppStateStore.updateAuthState(AuthState.AuthRequired)
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                LoginScreen()
            }
        }

        testRule.onNodeWithText(testRule.activity.getString(R.string.password_hint))
            .assertIsDisplayed()
    }

    @Test
    fun testPasswordHidden() {
        AppStateStore.updateAuthState(AuthState.AuthRequired)
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                LoginScreen()
            }
        }

        val passwordField =
            testRule.onNodeWithText(testRule.activity.getString(R.string.password_hint))
        passwordField.performTextInput("demo")

    }

    @Test
    fun testPasswordShown() {
        AppStateStore.updateAuthState(AuthState.AuthRequired)
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                LoginScreen()
            }
        }

        val passwordField =
            testRule.onNodeWithText(testRule.activity.getString(R.string.password_hint))
        passwordField.performTextInput("demo")
        testRule.onNodeWithContentDescription("switch password visibility").performClick()
        testRule.onNodeWithText("demo").assertIsDisplayed()
    }
}