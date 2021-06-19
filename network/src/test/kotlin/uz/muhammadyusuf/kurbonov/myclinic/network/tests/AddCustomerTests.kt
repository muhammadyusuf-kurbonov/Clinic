package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class AddCustomerTests : BaseTestClass() {
    @Test
    fun `customers - added successful`() {
        mockWebServer.enqueueResponse("customer-created.json", 201)
        runBlocking {
            appRepository.addNewCustomer(
                "Ivan",
                "Ivanov",
                "+998911112233"
            )
        }
    }

    @Test
    fun `customers - no phone`() {
        mockWebServer.enqueueResponse("customer-no-phone.json", 400)
        runBlocking {
            assertFailsWith<APIException> {
                appRepository.addNewCustomer(
                    "Ivan",
                    "Ivanov",
                    ""
                )
            }
        }
    }

    @Test
    fun `customers - no last name`() {
        mockWebServer.enqueueResponse("customer-created.json", 201)
        runBlocking {
            appRepository.addNewCustomer(
                "Ivan",
                "",
                "+998911112233"
            )
        }
    }

    @Test
    fun `customers - no first name`() {
        mockWebServer.enqueueResponse("customer-no-first-name.json", 400)
        runBlocking {
            assertFailsWith<APIException> {
                appRepository.addNewCustomer(
                    "",
                    "Ivanov",
                    "+998911112233"
                )
            }
        }
    }

    @Test
    fun `customers - connection closed`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-success.json", 200)
            mockWebServer.shutdown()
            assertFailsWith<NotConnectedException> {
                appRepository.addNewCustomer(
                    "",
                    "Ivanov",
                    "+998911112233"
                )
            }
        }
    }

    @Test
    fun `customers - connection timeout`() {
        runBlocking {
            assertFailsWith<NotConnectedException> {
                appRepository.addNewCustomer(
                    "",
                    "Ivanov",
                    "+998911112233"
                )
            }
        }
    }

}