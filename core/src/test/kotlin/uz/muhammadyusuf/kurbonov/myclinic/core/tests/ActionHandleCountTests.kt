package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.*
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationId
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType

@RunWith(JUnit4::class)
class ActionHandleCountTests {
    private val dummyCustomer = Customer(
        "123456789",
        "Ivan",
        "Ivanov",
        null,
        "+998913975538",
        0, null, null
    )

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `handle count - success`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } coAnswers {
                delay(1000)
                CommunicationId("test")
            }
        }


        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController1 = AppStatesController(this.coroutineContext, provider, repository)
            val testController2 = AppStatesController(this.coroutineContext, provider, repository)
            val testController3 = AppStatesController(this.coroutineContext, provider, repository)
            val testController4 = AppStatesController(this.coroutineContext, provider, repository)

            AppStatesController.pushAction(Action.Report(0, CallDirection.INCOMING, true))

            AppStateStore.reportState.assertEmitted {
                ReportState.Submitted == it
            }
            coVerify(exactly = 1) {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            }
        }
    }
}