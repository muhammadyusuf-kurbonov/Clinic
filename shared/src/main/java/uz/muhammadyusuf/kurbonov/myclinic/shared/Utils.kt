package uz.muhammadyusuf.kurbonov.myclinic.shared

import java.net.ConnectException

inline fun <reified T> attempts(count: Int, block: () -> T): T {
    var result: T? = null
    var e: Throwable? = null
    var currentIteration = 0
    while (result == null && currentIteration < count) {
        try {
            println("Try #$currentIteration: start")
            result = block()
            e = null
            println("Try #$currentIteration: success")
            break
        } catch (error: ConnectException) {
            println("Try #$currentIteration: error: $error")
            e = error
        } finally {
            currentIteration++
        }
    }
    if (e != null)
        throw e
    return result!!
}
