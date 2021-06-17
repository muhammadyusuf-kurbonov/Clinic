package uz.muhammadyusuf.kurbonov.myclinic.shared


import java.text.SimpleDateFormat
import java.util.*

fun Long.formatAsDate(
    pattern: String,
    newTimeZone: TimeZone = TimeZone.getDefault()
): String =
    SimpleDateFormat(pattern, Locale.getDefault()).apply {
        timeZone = newTimeZone
    }.format(Date(this))

fun Long.formatAsDate(): String = formatAsDate("dd MMM yyyy")

fun String.reformatDate(
    oldFormat: String,
    newFormat: String,
    oldTimeZone: TimeZone = TimeZone.getDefault(),
    newTimeZone: TimeZone = TimeZone.getDefault()
): String {
    val date = SimpleDateFormat(oldFormat, Locale.getDefault())
        .apply {
            timeZone = oldTimeZone
        }
        .parse(this)

    return date?.time?.formatAsDate(newFormat, newTimeZone)
        ?: throw IllegalArgumentException("Wrong pattern. Check it")
}

fun String.dateToSQLFormat() = reformatDate("dd MMM yyyy", "yyyy-MM-DD")

fun String.prettifyDate() = reformatDate("yyyy-MM-DD", "dd MMM YYYY")

inline fun <reified T> attempts(count: Int, onError: (Throwable) -> Unit = {}, block: () -> T): T {
    var result: T? = null
    var e: Throwable? = null
    var currentIteration = 0
    while (result == null && currentIteration < count) {
        try {
            printToConsole("Try #$currentIteration: start")
            result = block()
            e = null
            printToConsole("Try #$currentIteration: success")
            break
        } catch (error: Throwable) {
            error.printStackTrace()
            printToConsole("Try #$currentIteration: error: $error")
            onError(error)
            e = error
        } finally {
            currentIteration++
        }
    }
    if (e != null)
        throw e
    return result!!
}

lateinit var printToConsole: (msg: String) -> Unit

lateinit var recordException: (throwable: Throwable) -> Unit
