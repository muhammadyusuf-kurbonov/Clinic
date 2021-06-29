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
            AppStateStore.updateReportState(ReportState.PurposeRequested("test"))
            val appViewModel = AppStatesController(this.coroutineContext, provider, repository)
            appViewModel.handle(Action.SetPurpose("test"))

            AppStateStore.reportState.assertEmitted {
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
            AppStateStore.updateReportState(ReportState.PurposeRequested("test"))
            val mockkViewModel = AppStatesController(this.coroutineContext, provider, repository)
            mockkViewModel.handle(Action.SetPurpose("test"))

            AppStateStore.reportState.assertEmitted {
                it is ReportState.ConnectionFailed
            }
            coVerify {
                repository.updateCommunicationNote("test", "test")
            }
        }
    }
}
