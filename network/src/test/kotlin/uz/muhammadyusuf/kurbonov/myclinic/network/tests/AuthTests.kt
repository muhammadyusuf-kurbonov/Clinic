package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.APIException
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class AuthTests : BaseTestClass() {

    @Test
    fun `auth - successful`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-success.json", 200)
            val token = appRepository.authenticate("demo@32desk.com", "demo")
            @Suppress("SpellCheckingInspection")
            assertEquals(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6ImFjY2VzcyJ9.eyJ1c2VySWQiOiI1ZGMyYmVhYjBhZjljOWUzMGEwZWEwZjUiLCJpYXQiOjE2MTc5MzUwNDQsImV4cCI6MTY0OTQ3MTA0NCwiYXVkIjoiaHR0cHM6Ly95b3VyZG9tYWluLmNvbSIsImlzcyI6ImZlYXRoZXJzIiwic3ViIjoiYW5vbnltb3VzIiwianRpIjoiNjE4YjQ4OWMtYTk0Ni00OGU1LWEzYmUtNDljNDU5MDcwYmM1In0.CvdlfAxnyHybsoQ00OvL8EGNHygKQbkgqZRYPE0Ig40",
                token.token
            )
        }
    }

    @Test
    fun `auth - failed`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-failed.json", 401)
            assertFailsWith<AuthRequestException> {
                appRepository.authenticate("demo1@32desk.com", "demo")
            }
        }
    }

    @Test
    fun `auth - empty password`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-no-password.json", 400)
            assertFailsWith<APIException> {
                appRepository.authenticate("demo1@32desk.com", "demo")
            }
        }
    }

    @Test
    fun `auth - corrupted data`() {
        runBlocking {
            mockWebServer.enqueueResponse("corrupted.json", 200)
            assertFailsWith<NotConnectedException> {
                appRepository.authenticate("demo@32desk.com", "demo")
            }
        }
    }

    @Test
    fun `auth - connection closed`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-success.json", 200)
            mockWebServer.shutdown()
            assertFailsWith<NotConnectedException> {
                appRepository.authenticate("demo@32desk.com", "demo")
            }
        }
    }

    @Test
    fun `auth - connection timeout`() {
        runBlocking {
            assertFailsWith<NotConnectedException> {
                appRepository.authenticate("demo@32desk.com", "demo")
            }
        }
    }
}