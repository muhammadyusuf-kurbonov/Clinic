package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class CommunicationsTests : BaseTestClass() {
    @Test
    fun `communications - created`() {
        runBlocking {
            mockWebServer.enqueueResponse("communications-created.json", 201)
            val id = appRepository.sendCommunicationInfo(
                "609243304c1f68054f608398",
                CommunicationStatus.ACCEPTED,
                5,
                CommunicationType.INCOMING
            )
            assertEquals("60cd72ffa36151f9ddd27154", id.id)
        }
    }

    @Test
    fun `communications - bad-request test`() {
        runBlocking {
            mockWebServer.enqueueResponse("communications-bad-request.json", 400)
            assertFailsWith<APIException> {
                val id = appRepository.sendCommunicationInfo(
                    "609243304c1f68054f608398",
                    CommunicationStatus.ACCEPTED,
                    5,
                    CommunicationType.INCOMING
                )
                assertEquals("60cd72ffa36151f9ddd27154", id.id)
            }
        }
    }

    @Test
    fun `communications - invalid customerId`() {
        runBlocking {
            mockWebServer.enqueueResponse("communications-invalid-customerId.json", 400)
            assertFailsWith<APIException> {
                val id = appRepository.sendCommunicationInfo(
                    "609243304c1f6054f608398",
                    CommunicationStatus.ACCEPTED,
                    5,
                    CommunicationType.INCOMING
                )
                assertEquals("60cd72ffa36151f9ddd27154", id.id)
            }
        }
    }

    @Test
    fun `communications - corrupted data`() {
        runBlocking {
            mockWebServer.enqueueResponse("corrupted.json", 200)
            assertFailsWith<NotConnectedException> {
                appRepository.sendCommunicationInfo(
                    "609243304c1f68054f608398",
                    CommunicationStatus.ACCEPTED,
                    5,
                    CommunicationType.INCOMING
                )
            }
        }
    }

    @Test
    fun `communications - connection closed`() {
        runBlocking {
            mockWebServer.enqueueResponse("communications-created.json", 201)
            mockWebServer.shutdown()
            assertFailsWith<NotConnectedException> {
                appRepository.sendCommunicationInfo(
                    "609243304c1f68054f608398",
                    CommunicationStatus.ACCEPTED,
                    5,
                    CommunicationType.INCOMING
                )
            }
        }
    }

}