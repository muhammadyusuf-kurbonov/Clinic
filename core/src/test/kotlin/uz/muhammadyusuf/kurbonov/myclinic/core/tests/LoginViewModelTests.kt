package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
class LoginViewModelTests {
    @Test
    fun `login - success`() {
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "demo@32desk.com",
                    "demo"
                )
            } doReturn AuthToken("dummy")
        }

        val provider = mock<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo"))
            loginViewModel.authState.assertEmitted {
                AuthState.AuthSuccess == it
            }
            verify(provider).writePreference("token", "dummy")
        }
    }

    @Test
    fun `login - failed`() {
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "demo@32desk.com",
                    "demo123"
                )
            } doAnswer {
                throw AuthRequestException()
            }
        }

        val provider = mock<SystemFunctionProvider> {
        }

        runBlocking {
            val loginViewModel = AppViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.Login("demo@32desk.com", "demo123"))
            loginViewModel.authState.assertEmitted {
                AuthState.AuthFailed == it
            }
            verify(provider).writePreference("token", "")
        }
    }

    @Test
    fun `login - connectionError`() {
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "demo@32desk.com",
                    "demo123"
                )
            } doAnswer {
                throw NotConnectedException()
            }
        }

        val provider = mock<SystemFunctionProvider> {
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
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "demo@32desk.com",
                    "demo"
                )
            } doAnswer {
                throw APIException(400, "field required")
            }
        }

        val provider = mock<SystemFunctionProvider> {
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
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "",
                    "demo"
                )
            } doAnswer {
                throw APIException(400, "field required")
            }
        }

        val provider = mock<SystemFunctionProvider> {
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
        val repository = mock<AppRepository> {
            onBlocking {
                authenticate(
                    "demo@32desk.com",
                    ""
                )
            } doAnswer {
                throw APIException(400, "field required")
            }
        }

        val provider = mock<SystemFunctionProvider> {
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