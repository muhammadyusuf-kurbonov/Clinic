package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

@RunWith(JUnit4::class)
class LogoutTests {
    @Test
    fun `logout - success`() {
        val repository = mockk<AppRepository>(relaxed = true) {
        }

        val provider = mockk<SystemFunctionsProvider> {
            coEvery {
                writePreference(any(), any())
            } just Runs
        }

        runBlocking {
            val statesController = AppStatesController(this.coroutineContext, provider, repository)
            statesController.handle(Action.Logout)
            AppStateStore.authState.assertEmitted {
                AuthState.AuthRequired == it
            }
            coVerify {
                provider.writePreference("token", "")
                repository.token = ""
            }
        }
    }
}