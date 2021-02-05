package uz.muhammadyusuf.kurbonov.myclinic.utils

inline fun <reified T> retries(count: Int, block: () -> T): T {
    var result: T? = null
    var currentIteration = 0
    while (result == null && currentIteration < count) {
        try {
            result = block()
        } catch (e: Exception) {

        } finally {
            currentIteration++
        }
    }
    return result ?: throw RetriesExpiredException(count)
}

class RetriesExpiredException(retriesCount: Int) :
    Exception("All retries ($retriesCount) are failed")