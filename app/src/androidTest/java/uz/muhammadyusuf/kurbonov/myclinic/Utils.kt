package uz.muhammadyusuf.kurbonov.myclinic

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
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

internal fun checkNotificationWithText(uiDevice: UiDevice, text: String) {
    uiDevice.wait(Until.findObject(By.text(text)), 15000)

    val notification = uiDevice.findObject(By.text(text))
    TestCase.assertEquals(text, notification.text)
}

internal suspend fun <T> StateFlow<T>.waitUntil(
    timeout: Long,
    condition: (T) -> Boolean
) {
    Assert.assertThrows(SuccessException::class.java) {
        runBlocking {
            withTimeout(timeout) {
                collect {
                    Log.d("flow_$this", "received new value $it")
                    if (condition(it))
                        throw SuccessException()
                }
            }
        }
    }
}

class SuccessException : CancellationException()