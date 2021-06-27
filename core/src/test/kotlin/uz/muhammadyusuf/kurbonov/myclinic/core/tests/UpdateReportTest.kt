package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException

@RunWith(JUnit4::class)
class UpdateReportTest {
    @Test
    fun `update report - success`() {
        val repository = mockk<AppRepository> {
            coEvery {
                updateCommunicationNote(any(), any())
            } just Runs

        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val appViewModel = AppViewModel(this.coroutineContext, provider, repository)
            appViewModel._reportState.value = ReportState.PurposeRequested("test")
            appViewModel.handle(Action.SetPurpose("test"))

            appViewModel.reportState.assertEmitted {
                it is ReportState.Submitted
            }

            coVerify {
                repository.updateCommunicationNote("test", "test")
            }
        }
    }

    @Test
    fun `update report - connection error`() {
        val repository = mockk<AppRepository> {
            coEvery {
                updateCommunicationNote(any(), any())
            } throws NotConnectedException()

        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            val mockkViewModel = AppViewModel(this.coroutineContext, provider, repository)
            mockkViewModel._reportState.value = ReportState.PurposeRequested("test")
            mockkViewModel.handle(Action.SetPurpose("test"))

            mockkViewModel.reportState.assertEmitted {
                it is ReportState.ConnectionFailed
            }
            coVerify {
                repository.updateCommunicationNote("test", "test")
            }
        }
    }
}