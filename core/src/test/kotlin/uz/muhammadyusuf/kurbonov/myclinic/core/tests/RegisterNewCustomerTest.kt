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
import uz.muhammadyusuf.kurbonov.myclinic.core.states.RegisterState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException

@RunWith(JUnit4::class)
class RegisterNewCustomerTest {
    @Test
    fun `register customer - success`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                addNewCustomer(
                    any(),
                    any(),
                    any()
                )
            } just Runs
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.RegisterNewCustomer("Test", "Testov", "+998911234567"))
            AppStateStore.registerState.assertEmitted {
                RegisterState.RegisterSuccess == it
            }
            coVerify {
                repository.addNewCustomer(
                    "Test",
                    "Testov",
                    "+998911234567"
                )
            }
        }
    }

    @Test
    fun `register customer - first name validation failed`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                addNewCustomer(
                    any(),
                    any(),
                    any()
                )
            } just Runs
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.RegisterNewCustomer("", "Testov", "+998911234567"))
            AppStateStore.registerState.assertEmitted {
                RegisterState.VerificationFailed == it
            }
            coVerify(exactly = 0) {
                repository.addNewCustomer(
                    "Test",
                    "Testov",
                    "+998911234567"
                )
            }
        }
    }

    @Test
    fun `register customer - phone validation failed`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                addNewCustomer(
                    any(),
                    any(),
                    any()
                )
            } just Runs
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.RegisterNewCustomer("Test", "Testov", ""))
            AppStateStore.registerState.assertEmitted {
                RegisterState.VerificationFailed == it
            }
            coVerify(exactly = 0) {
                repository.addNewCustomer(
                    "Test",
                    "Testov",
                    "+998911234567"
                )
            }
        }
    }

    @Test
    fun `register customer - connection error`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                addNewCustomer(
                    any(),
                    any(),
                    any()
                )
            } throws NotConnectedException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.RegisterNewCustomer("Test", "Testov", "+998911234567"))
            AppStateStore.registerState.assertEmitted {
                RegisterState.ConnectionFailed == it
            }
            coVerify {
                repository.addNewCustomer(
                    "Test",
                    "Testov",
                    "+998911234567"
                )
            }
        }
    }

    @Test
    fun `register customer - auth token expired`() {
        val repository = mockk<AppRepository>(relaxed = true) {
            coEvery {
                addNewCustomer(
                    any(),
                    any(),
                    any()
                )
            } throws AuthRequestException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val loginViewModel = AppStatesController(this.coroutineContext, provider, repository)
            loginViewModel.handle(Action.RegisterNewCustomer("Test", "Testov", "+998911234567"))
            AppStateStore.registerState.assertEmitted {
                RegisterState.Default == it
            }
            AppStateStore.authState.assertEmitted {
                AuthState.AuthRequired == it
            }
            coVerify {
                repository.addNewCustomer(
                    "Test",
                    "Testov",
                    "+998911234567"
                )
            }
        }
    }
}