package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class UserTests : BaseTestClass() {
    @Test
    fun `user - success`() {
        mockWebServer.enqueueResponse("user-get-found.json", 200)
        runBlocking {
            val treatment = appRepository.getUser("5dc2beab0af9c9e30a0ea0f5")
            assertEquals("5dc2beab0af9c9e30a0ea0f5", treatment._id)
            assertEquals("Anor", treatment.firstName)
        }
    }


    @Test
    fun `user - error`() {
        mockWebServer.enqueueResponse("user-get-error.json", 500)
        runBlocking {
            assertFailsWith<APIException> {
                val treatment = appRepository.getUser("5dc3c84d9a76124f537464fe")
                assertEquals("5dc2beab0af9c9e30a0ea0f5", treatment._id)
                assertEquals("Anor", treatment.firstName)
            }
        }
    }
}