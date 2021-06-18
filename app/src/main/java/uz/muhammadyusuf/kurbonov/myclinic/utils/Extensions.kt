package uz.muhammadyusuf.kurbonov.myclinic.utils

import android.os.Environment
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.shared.formatAsDate
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileDebugTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        val priorityString: String = when (priority) {
            Log.ASSERT -> "ASSERT"
            Log.DEBUG -> "DEBUG"
            Log.ERROR -> "ERROR"
            Log.INFO -> "INFO"
            Log.VERBOSE -> "VERBOSE"
            Log.WARN -> "WARNING"
            else -> "INFO"
        }
        // Crashlitics logging
        if (t != null)
            FirebaseCrashlytics.getInstance().recordException(t)
        else
            FirebaseCrashlytics.getInstance().log(
                "$priorityString::$tag::$message"
            )

        // File logging
        try {
            val directory =
                Environment.getExternalStoragePublicDirectory("${Environment.DIRECTORY_DOCUMENTS}/logs")

            if (!directory.exists())
                directory.mkdirs()

            val fileName = "myLog.txt"

            val file = File("${directory.absolutePath}${File.separator}$fileName")

            if (file.length() >= 2 * 1024 * 1024) {
                file.delete()
            }

            file.createNewFile()

            if (file.exists()) {
                val fos = FileOutputStream(file, true)

                val dateTime = System.currentTimeMillis().formatAsDate("yyyy-MM-dd HH:mm:ss")

                fos.write("$dateTime $message\n".toByteArray(Charsets.UTF_8))
                fos.close()
            }

        } catch (e: IOException) {
            Log.println(Log.ERROR, "FileLogTree", "Error while logging into file: $e")
        }
    }
}

