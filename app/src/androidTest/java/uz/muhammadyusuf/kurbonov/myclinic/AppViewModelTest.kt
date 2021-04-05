package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

@RunWith(AndroidJUnit4::class)
class AppViewModelTest {

    @Before
    fun prepare() {
        val context = InstrumentationRegistry.getInstrumentation().context
        App.appViewModel.reduceBlocking(Action.Start(context))

        val authService by inject(APIService::class.java)
        runBlocking {
            val response = authService.authenticate(
                AuthRequest(
                    email = "demo@32desk.com",
                    password = "demo"
                )
            )

            if (response.isSuccessful) {
                App.pref.edit()
                    .putString("token", response.body()?.accessToken)
                    .apply()
            }
        }
    }

    @Test
    fun testInitialization() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduceBlocking(Action.Start(context))
            assert(true)
        }
    }

    @Test
    fun testFoundCustomer() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            App.appViewModel.reduce(Action.Search("+998994801416"))
            assertTrue(
                "State is ${App.appViewModel.state.value}",
                App.appViewModel.state.value is State.Found
            )
        }
    }

    @Test
    fun testCustomerNotFound() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            App.appViewModel.reduce(Action.Search("+99894801416"))
            assertEquals(State.NotFound, App.appViewModel.state.value)
        }
    }

    @Test
    fun testNoConnection() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            App.appViewModel.reduce(Action.SetNoConnectionState)
            assertEquals(State.ConnectionError, App.appViewModel.state.value)
        }
    }
}