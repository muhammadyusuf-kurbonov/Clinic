package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

@RunWith(JUnit4::class)
class StateSavingTests {
    @Test
    fun `saving state`() {
        val map = hashMapOf<String, Any>()
        val repository = mockk<AppRepository>()
        val provider = mockk<SystemFunctionsProvider> {
            coEvery {
                writePreference(any(), any())
            } answers {
                map[args[0] as String] = args[1] as Any
            }
        }
        runBlocking {
            val viewModel = AppViewModel(coroutineContext, provider, repository)
            viewModel._authState.value = AuthState.AuthSuccess
            viewModel.saveStates()
        }
        coVerify {
            provider.writePreference("authState", AuthState.AuthSuccess)
            provider.writePreference("customerState", CustomerState.Default)
            provider.writePreference("reportState", ReportState.Default)
        }
    }

    @Test
    fun `restoring state`() {
        val repository = mockk<AppRepository>()
        val provider = mockk<SystemFunctionsProvider> {
            coEvery {
                readPreference<AuthState>("authState", any())
            } returns AuthState.AuthFailed
            coEvery {
                readPreference<CustomerState>("customerState", any())
            } returns CustomerState.NotFound
            coEvery {
                readPreference<ReportState>("reportState", any())
            } returns ReportState.ConnectionFailed
        }
        runBlocking {
            val viewModel = AppViewModel(coroutineContext, provider, repository)
            viewModel._authState.value = AuthState.AuthSuccess
            viewModel.restoreFromState()
            viewModel.authState.assertEmitted {
                it == AuthState.AuthFailed
            }
            viewModel.customerState.assertEmitted {
                it == CustomerState.NotFound
            }
            viewModel.reportState.assertEmitted {
                it == ReportState.ConnectionFailed
            }
        }
    }
}