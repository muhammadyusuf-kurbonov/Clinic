package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.AuthRequestException
import uz.muhammadyusuf.kurbonov.myclinic.network.CustomerNotFoundException
import uz.muhammadyusuf.kurbonov.myclinic.network.NotConnectedException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class SearchTests : BaseTestClass() {
    @Test
    fun `search - customer found`() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 200)
            val customer = appRepository.search("+998903500490")
            assertEquals(1, customer.total)
            assertEquals("Иван", customer.data[0].first_name)
        }
    }

    @Test
    fun `search - not Found With Code 200`() {
        runBlocking {
            mockWebServer.enqueueResponse("not-found.json", 200)
            assertFailsWith<CustomerNotFoundException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }

    @Test
    fun `search - not Found With Code 404`() {
        runBlocking {
            mockWebServer.enqueueResponse("not-found.json", 404)
            assertFailsWith<CustomerNotFoundException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }

    @Test
    fun `search - access denied`() {
        runBlocking {
            mockWebServer.enqueueResponse("authentication-failed.json", 401)
            assertFailsWith<AuthRequestException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }

    @Test
    fun `search - corrupted data`() {
        runBlocking {
            mockWebServer.enqueueResponse("corrupted.json", 200)
            assertFailsWith<NotConnectedException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }

    @Test
    fun `search - connection closed`() {
        runBlocking {
            mockWebServer.enqueueResponse("corrupted.json", 200)
            mockWebServer.shutdown()
            assertFailsWith<NotConnectedException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }

    @Test
    fun `search - connection timeout`() {
        runBlocking {
            assertFailsWith<NotConnectedException> {
                val customer = appRepository.search("+998903500490")
                assertEquals(0, customer.total)
            }
        }
    }
}