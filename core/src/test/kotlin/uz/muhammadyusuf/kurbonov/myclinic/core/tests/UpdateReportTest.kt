package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationId
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType

@RunWith(JUnit4::class)
class UpdateReportTest {
    private val dummyCustomer = Customer(
        "123456789",
        "Ivan",
        "Ivanov",
        null,
        "+998913975538",
        null, null
    )

    // State flow is difficult to mock or spy. And because of that this method has 2 mocks
    @Test
    fun `update report - success`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns CommunicationId("test")

            coEvery {
                updateCommunicationNote(any(), any())
            } just Runs

        }

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val mockkViewModel = spyk(AppViewModel(this.coroutineContext, provider, repository)) {
                every { customerState.value } returns CustomerState.Found(dummyCustomer)
            }
            mockkViewModel.handle(Action.Report(10, CallDirection.INCOMING, false))
            // this is for set reportState purposeRequired

            mockkViewModel.handle(Action.SetPurpose("test"))

            mockkViewModel.reportState.assertEmitted {
                it is ReportState.Submitted
            }

            coVerify {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.ACCEPTED,
                    10,
                    CommunicationType.INCOMING
                )

                repository.updateCommunicationNote("test", "test")
            }
        }
    }

    @Test
    fun `update report - connection error`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns CommunicationId("test")

            coEvery {
                updateCommunicationNote(any(), any())
            } throws NotConnectedException()

        }

        val provider = mockk<SystemFunctionProvider> {
        }

        runBlocking {
            val mockkViewModel = spyk(AppViewModel(this.coroutineContext, provider, repository)) {
                every { customerState.value } returns CustomerState.Found(dummyCustomer)
            }
            mockkViewModel.handle(Action.Report(10, CallDirection.INCOMING, false))
            // reportState is PurposeRequired

            mockkViewModel.handle(Action.SetPurpose("test"))

            mockkViewModel.reportState.assertEmitted {
                it is ReportState.ConnectionFailed
            }
            coVerify {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.ACCEPTED,
                    10,
                    CommunicationType.INCOMING
                )

                repository.updateCommunicationNote("test", "test")
            }
        }
    }
}