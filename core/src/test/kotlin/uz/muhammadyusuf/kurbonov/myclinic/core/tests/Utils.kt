package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeout
import java.nio.charset.StandardCharsets
import kotlin.test.assertFailsWith

internal class SuccessException : Exception()

suspend fun <T> Flow<T>.assertEmitted(timeout: Long = 5000, expression: (T) -> Boolean) {
    assertFailsWith<SuccessException> {
        withTimeout(timeout) {
            collect {
                if (expression(it))
                    throw SuccessException()
            }
        }
    }
}

internal fun Any.getJSON(fileName: String): String {
    val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
        ?: throw IllegalArgumentException("Resource not found")

    val source = inputStream.let { inputStream.buffered() }
    return source.bufferedReader(StandardCharsets.UTF_8).readText()
}
