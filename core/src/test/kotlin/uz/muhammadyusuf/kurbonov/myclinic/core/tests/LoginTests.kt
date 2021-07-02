package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class LoginTests {
    @Test
    fun `login - success`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    "demo"
                )
            } returns AuthToken("dummy")
        }

        val provider = mockk<SystemFunctionsProvider> {
            coEvery {
                writePreference(any(), any())
            } just Runs
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo"))
            AppStateStore.authState.assertEmitted {
                AuthState.AuthSuccess == it
            }
            coVerify { provider.writePreference("token", "dummy") }
        }
    }

    @Test
    fun `login - failed`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    "demo123"
                )
            } throws AuthRequestException()
        }

        val provider = mockk<SystemFunctionsProvider> {
            coEvery {
                writePreference(any(), any())
            } just Runs
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo123"))
            AppStateStore.authState.assertEmitted {
                AuthState.AuthFailed == it
            }
            coVerify { provider.writePreference("token", "") }
        }
    }

    @Test
    fun `login - connectionError`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    "demo123"
                )
            } throws NotConnectedException()

        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val controller = AppStatesController(this.coroutineContext, provider, repository)
            controller.handle(Action.Login("demo@32desk.com", "demo123"))
            AppStateStore.authState.assertEmitted {
                AuthState.ConnectionFailed == it
            }
        }
    }

    @Test
    fun `login - api error`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    "demo"
                )
            } throws APIException(400, "field required")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        assertFailsWith<APIException> {
            runBlocking {
                val loginViewModel =
                    AppStatesController(this.coroutineContext, provider, repository)
                loginViewModel.handle(Action.Login("demo@32desk.com", "demo"))
                runBlocking { delay(2000) }
            }
        }
    }

    @Test
    fun `login - empty username`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "",
                    "demo"
                )
            } throws APIException(400, "field required")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("", "demo"))
            AppStateStore.authState.assertEmitted {
                it == AuthState.ValidationFailed
            }
        }

    }

    @Test
    fun `login - invalid username`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo123",
                    "demo"
                )
            } throws APIException(400, "field required")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo123", "demo"))
            AppStateStore.authState.assertEmitted {
                it == AuthState.ValidationFailed
            }
        }

    }

    @Test
    fun `login - empty password`() {
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    ""
                )
            } throws APIException(400, "field required")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", ""))
            AppStateStore.authState.assertEmitted {
                it == AuthState.ValidationFailed
            }
        }

    }
}