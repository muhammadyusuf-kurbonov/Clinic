package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class TreatmentTests : BaseTestClass() {
    @Test
    fun `treatment - found`() {
        mockWebServer.enqueueResponse("treatment-get-found.json", 200)
        runBlocking {
            val treatment = appRepository.getTreatment("5dc3c84d9a76124f537464fe")
            assertEquals("5dc3c84d9a76124f537464fe", treatment._id)
            assertEquals("Д003", treatment.code)
        }
    }

    @Test
    fun `treatment - error`() {
        mockWebServer.enqueueResponse("treatment-get-error.json", 500)
        runBlocking {
            assertFailsWith<APIException> {
                val treatment = appRepository.getTreatment("5dc3c84d9a76124f537464fe")
                assertEquals("5dc3c84d9a76124f537464fe", treatment._id)
                assertEquals("Д003", treatment.code)
            }
        }
    }
}