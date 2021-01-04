package uz.muhammadyusuf.kurbonov.myclinic

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Before
    fun test_queue() {
        val executor = Executors.newSingleThreadExecutor()
        val a = arrayListOf("abcd", "bagnanai", "cllassss", "dkkajjdu")
        val s = StringBuilder()
        a.forEach { str ->
            executor.submit {
                print(System.currentTimeMillis())
                val time = Random.nextInt(5)
                runBlocking {
                    delay(time * 1000L)
                    println("Ended for $str")
                }
            }
        }
        print(s.toString())
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun queue_test() {
        val executor = Executors.newSingleThreadExecutor()
        val a = arrayListOf("abcd", "bagnanai", "cllassss", "dkkajjdu")
        val s = StringBuilder()
        a.forEach { str ->
            executor.submit {
                print(System.currentTimeMillis())
                val time = Random.nextInt(5)
                runBlocking {
                    delay(time * 1000L)
                    println("Ended for $str")
                }
            }
        }
        print(s.toString())
    }
}