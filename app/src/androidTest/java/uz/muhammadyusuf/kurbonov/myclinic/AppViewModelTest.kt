package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

@RunWith(AndroidJUnit4::class)
class AppViewModelTest {


    @Before
    fun prepare() {
        val context = InstrumentationRegistry.getInstrumentation().context
        App.appViewModel.reduceBlocking(Action.Start(context))
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
            App.appViewModel.reduce(Action.Search("+998994801416"))
            assertTrue(App.appViewModel.state.value is State.Found)
        }
    }

    @Test
    fun testCustomerNotFound() {
        runBlocking {
            App.appViewModel.reduce(Action.Search("+99894801416"))
            assertEquals(State.NotFound, App.appViewModel.state.value)
        }
    }

    @Test
    fun testNoConnection() {
        runBlocking {
            App.appViewModel.reduce(Action.SetNoConnectionState)
            assertEquals(State.ConnectionError, App.appViewModel.state.value)
        }
    }
}