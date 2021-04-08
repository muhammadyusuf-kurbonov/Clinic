package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AppViewModelTest {

    @Before
    fun prepare() {
        val context = InstrumentationRegistry.getInstrumentation().context
        App.appViewModel.reduceBlocking(Action.Start(context))

        val authService by lazy {
            DI.getAPIService()
        }
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
    fun testAddNewUser() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            App.appViewModel.reduce(Action.Search("+998945886633"))
            assertTrue(App.appViewModel.state.value is State.NotFound)

            App.appViewModel.reduce(Action.EndCall("+998945886633"))
            assertTrue(App.appViewModel.state.value is State.AddNewCustomerRequest)
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

    @Test
    fun zTestMainWorkerLifecycle() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))
            var info = WorkManager.getInstance(context).getWorkInfosForUniqueWork("main_work").get()
            info.forEach {
                println("${it.id} is ${it.state}")
                assert(!it.state.isFinished)
            }
            delay(2500)
            App.appViewModel.reduce(Action.Finish)
            info = WorkManager.getInstance(context).getWorkInfosForUniqueWork("main_work").get()
            info.forEach {
                println("${it.id} is ${it.state}")
                assert(it.state.isFinished)
            }
        }
    }
}