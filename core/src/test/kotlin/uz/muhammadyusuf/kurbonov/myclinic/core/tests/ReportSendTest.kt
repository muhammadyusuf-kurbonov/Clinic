package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.core.*
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationId
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class ReportSendTest {
    private val dummyCustomer = Customer(
        "123456789",
        "Ivan",
        "Ivanov",
        null,
        "+998913975538",
        0, null, null
    )

    @Test
    fun `report - missed call with found customer`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns CommunicationId("test")
        }


        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(0, CallDirection.INCOMING, true))
            AppStateStore.reportState.assertEmitted {
                ReportState.Submitted == it
            }
            coVerify {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            }
        }
    }

    @Test
    fun `report - call with not found customer`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            } returns CommunicationId("test")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.NotFound)
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(0, CallDirection.INCOMING, true))
            AppStateStore.reportState.assertEmitted {
                ReportState.AskToAddNewCustomer == it
            }
        }
    }

    @Test
    fun `report - call with no connection while customer search`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            } returns CommunicationId("test")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.ConnectionFailed)
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(0, CallDirection.INCOMING, true))
            AppStateStore.reportState.assertEmitted {
                ReportState.ConnectionFailed == it
            }
        }
    }

    @Test
    fun `report - connection error while send`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            } throws NotConnectedException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(0, CallDirection.INCOMING, true))
            AppStateStore.reportState.assertEmitted {
                ReportState.ConnectionFailed == it
            }
        }
    }

    @Test
    fun `report - expired token while send`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            } throws AuthRequestException()
        }

        val provider = mockk<SystemFunctionsProvider> {
        }


        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(0, CallDirection.INCOMING, true))
            AppStateStore.reportState.assertEmitted {
                ReportState.Default == it
            }
            AppStateStore.authState.assertEmitted {
                AuthState.AuthRequired == it
            }
        }

    }

    @Test
    fun `report - api error while send`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.MISSED,
                    0,
                    CommunicationType.INCOMING
                )
            } throws APIException(400, "test")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        assertFailsWith<APIException> {
            runBlocking {
                AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
                val testController =
                    AppStatesController(this.coroutineContext, provider, repository)
                testController.handle(Action.Report(0, CallDirection.INCOMING, true))
                AppStateStore.reportState.assertEmitted {
                    ReportState.ConnectionFailed == it
                }
            }
        }
    }

    @Test
    fun `report - accepted incoming call with found customer`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns CommunicationId("test")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(10, CallDirection.INCOMING, false))
            AppStateStore.reportState.assertEmitted {
                it is ReportState.PurposeRequested
            }
            coVerify {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.ACCEPTED,
                    10,
                    CommunicationType.INCOMING
                )
            }
        }
    }

    @Test
    fun `report - accepted outgoing call with found customer`() {
        val repository = mockk<AppRepository> {
            coEvery {
                sendCommunicationInfo(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns CommunicationId("test")
        }

        val provider = mockk<SystemFunctionsProvider> {
        }

        runBlocking {
            AppStateStore.updateCustomerState(CustomerState.Found(dummyCustomer))
            val testController = AppStatesController(this.coroutineContext, provider, repository)
            testController.handle(Action.Report(10, CallDirection.OUTGOING, false))
            AppStateStore.reportState.assertEmitted {
                it is ReportState.PurposeRequested
            }
            coVerify {
                repository.sendCommunicationInfo(
                    "123456789",
                    CommunicationStatus.ACCEPTED,
                    10,
                    CommunicationType.OUTGOING
                )
            }
        }
    }
}