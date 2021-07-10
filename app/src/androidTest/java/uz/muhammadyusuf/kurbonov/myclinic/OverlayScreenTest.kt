package uz.muhammadyusuf.kurbonov.myclinic

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.screens.OverlayScreen
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

@RunWith(AndroidJUnit4::class)
class OverlayScreenTest {
    @get: Rule
    val testRule = createAndroidComposeRule<MainActivity>()

    private val scope = CoroutineScope(Dispatchers.Main)

    @ExperimentalAnimationApi
    @Before
    fun setUp() {
        testRule.setContent {
            CompositionLocalProvider(
                LocalAppControllerProvider provides AppStatesController(
                    scope.coroutineContext,
                    SystemFunctionsProvider(),
                    AppRepository("12346789")
                ),
                LocalPhoneNumberProvider provides "+998913975538"
            ) {
                OverlayScreen()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testSearching() = runBlockingTest {
        AppStateStore.updateAuthState(AuthState.AuthSuccess)
        AppStateStore.updateCustomerState(CustomerState.Searching)

        testRule.onNodeWithText(
            testRule.activity
                .getString(R.string.searching), ignoreCase = true
        ).assertIsDisplayed()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testNotFoundTextDisplayed() = runBlockingTest {
        AppStateStore.updateAuthState(AuthState.AuthSuccess)
        AppStateStore.updateCustomerState(CustomerState.NotFound)

        testRule.onNodeWithText(
            testRule.activity
                .getString(R.string.not_found), ignoreCase = true
        ).assertIsDisplayed()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testNotConnectedButtonDisplayed() = runBlockingTest {
        AppStateStore.updateAuthState(AuthState.AuthSuccess)
        AppStateStore.updateCustomerState(CustomerState.ConnectionFailed)

        testRule.onNodeWithText(
            testRule.activity
                .getString(R.string.retry), ignoreCase = true
        ).assertIsDisplayed()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testNotConnectedLabelDisplayed() = runBlockingTest {
        AppStateStore.updateAuthState(AuthState.AuthSuccess)
        AppStateStore.updateCustomerState(CustomerState.ConnectionFailed)

        testRule.onNodeWithText(
            testRule.activity
                .getString(R.string.no_internet_connection), ignoreCase = true
        ).assertIsDisplayed()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testFound() = runBlockingTest {
        AppStateStore.updateAuthState(AuthState.AuthSuccess)
        AppStateStore.updateCustomerState(
            CustomerState.Found(
                Customer(
                    id = "123",
                    "Ivan",
                    "Ivanov",
                    null,
                    "+998913975538",
                    15000,
                    null, null
                )
            )
        )

        testRule.onNodeWithText(
            "Ivan", substring = true
        ).assertIsDisplayed()

        testRule.onNodeWithText(
            "Ivanov", substring = true
        ).assertIsDisplayed()

        testRule.onNodeWithText(
            "+998913975538", substring = true
        ).assertIsDisplayed()
    }
}