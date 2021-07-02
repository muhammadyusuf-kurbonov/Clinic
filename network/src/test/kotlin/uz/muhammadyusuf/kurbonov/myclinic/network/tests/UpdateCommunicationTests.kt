package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class UpdateCommunicationTests : BaseTestClass() {
    @Test
    fun `updateCommunication - successful`() {
        mockWebServer.enqueueResponse("communications-updated.json", 200)
        runBlocking {
            appRepository.updateCommunicationNote("60cd72ffa36151f9ddd27154", "Help")
        }
    }

    @Test
    fun `updateCommunication - updated not existing record`() {
        mockWebServer.enqueueResponse("communications-invalid-id.json", 400)
        runBlocking {
            assertFailsWith<APIException> {
                appRepository.updateCommunicationNote("60cd72ffa36151f9ddd2715", "Help")
            }
        }
    }

    @Test
    fun `updateCommunication - connection closed`() {
        runBlocking {
            mockWebServer.enqueueResponse("communications-updated.json", 200)
            mockWebServer.shutdown()
            assertFailsWith<NotConnectedException> {
                appRepository.updateCommunicationNote(
                    "60cd72ffa36151f9ddd27154",
                    "Help"
                )
            }
        }
    }

}