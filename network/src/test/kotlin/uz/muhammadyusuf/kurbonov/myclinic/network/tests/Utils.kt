package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.nio.charset.StandardCharsets

internal fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
    val inputStream = javaClass.classLoader?.getResourceAsStream("api-response/$fileName")
        ?: throw IllegalArgumentException("Resource not found")

    val source = inputStream.let { inputStream.buffered() }
    enqueue(
        MockResponse()
            .setResponseCode(code)
            .setBody(source.bufferedReader(StandardCharsets.UTF_8).readText())
    )
}