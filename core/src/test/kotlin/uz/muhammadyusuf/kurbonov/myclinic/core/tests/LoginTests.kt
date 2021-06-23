package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
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
        val repository = mockk<AppRepository> {
            coEvery {
                authenticate(
                    "demo@32desk.com",
                    "demo"
                )
            } returns AuthToken("dummy")
        }

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo"))
            loginViewModel.authState.assertEmitted {
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

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo123"))
            loginViewModel.authState.assertEmitted {
                AuthState.AuthRequired == it
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

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo123"))
            loginViewModel.authState.assertEmitted {
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

        val provider = mockk<SystemFunctionProvider> {
        }

        assertFailsWith<APIException> {
            runBlocking {
                val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
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

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("", "demo"))
            loginViewModel.authState.assertEmitted {
                it is AuthState.FieldRequired && it.fieldName == "username"
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

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", ""))
            loginViewModel.authState.assertEmitted {
                it is AuthState.FieldRequired && it.fieldName == "password"
            }
        }

    }
}