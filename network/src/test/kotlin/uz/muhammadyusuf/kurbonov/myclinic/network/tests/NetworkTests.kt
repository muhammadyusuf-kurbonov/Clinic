package uz.muhammadyusuf.kurbonov.myclinic.network.tests.uz.muhammadyusuf.kurbonov.myclinic.network.tests

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import uz.muhammadyusuf.kurbonov.myclinic.shared.recordException
import java.io.EOFException
import java.nio.charset.StandardCharsets
import kotlin.test.assertIs

@RunWith(JUnit4::class)
class NetworkTests {
    private val mockWebServer = MockWebServer()
    private lateinit var appRepository: AppRepository

    @Before
    fun initialize() {
        mockWebServer.start()
        appRepository = AppRepository("dummy", mockWebServer.url("/").url().toString())
        printToConsole = {
            println(it)
        }
        recordException = {
            it.printStackTrace()
        }
    }

    @After
    fun dismiss() {
        mockWebServer.shutdown()
        printToConsole("End test")
    }


    @Test
    fun testNotFound() {
        runBlocking {
            mockWebServer.enqueueResponse("not-found.json", 404)
            val response = appRepository.search("+998911234567")
            assertIs<SearchResult.NotFound>(response)
        }
    }

    @Test
    fun testFound() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 200)
            val response = appRepository.search("+998911234567")
            assertIs<SearchResult.Found>(response)
        }
    }

    @Test(expected = EOFException::class)
    fun testCorrupted() {
        runBlocking {
            repeat(13) {
                mockWebServer.enqueueResponse("corrupted.json", 200)
            }
            val response = appRepository.search("+998911234567")
            assertIs<SearchResult.UnknownError>(response)
        }
    }

    @Test
    fun testAuthRequest() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 401)
            val response = appRepository.search("+998911234567")
            assertIs<SearchResult.AuthRequested>(response)
        }
    }

    @Test
    fun testNoResponse() {
        runBlocking {
            mockWebServer.shutdown()
            val response = appRepository.search("+998911234567")
            assertIs<SearchResult.NoConnection>(response)
        }
    }


    private fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
        val inputStream = javaClass.classLoader?.getResourceAsStream("api-response/$fileName")
            ?: throw IllegalArgumentException("Resource not found")

        val source = inputStream.let { inputStream.buffered() }
        enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(source.bufferedReader(StandardCharsets.UTF_8).readText())
        )
    }
}