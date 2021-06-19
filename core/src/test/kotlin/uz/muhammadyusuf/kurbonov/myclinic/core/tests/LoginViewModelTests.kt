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
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.login.LoginActions
import uz.muhammadyusuf.kurbonov.myclinic.core.login.LoginStates
import uz.muhammadyusuf.kurbonov.myclinic.core.login.LoginViewModel
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
            val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(LoginActions.Login("demo@32desk.com", "demo"))
            loginViewModel.state.assertEmitted {
                LoginStates.AuthSuccess == it
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
            val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(LoginActions.Login("demo@32desk.com", "demo123"))
            loginViewModel.state.assertEmitted {
                LoginStates.AuthFailed == it
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
            val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(LoginActions.Login("demo@32desk.com", "demo123"))
            loginViewModel.state.assertEmitted {
                LoginStates.ConnectionFailed == it
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
                val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
                loginViewModel.handle(LoginActions.Login("demo@32desk.com", "demo"))
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
            val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(LoginActions.Login("", "demo"))
            loginViewModel.state.assertEmitted {
                it is LoginStates.FieldRequired && it.fieldName == "username"
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
            val loginViewModel = LoginViewModel(this.coroutineContext, provider, repository)
            loginViewModel.handle(LoginActions.Login("demo@32desk.com", ""))
            loginViewModel.state.assertEmitted {
                it is LoginStates.FieldRequired && it.fieldName == "password"
            }
        }

    }
}