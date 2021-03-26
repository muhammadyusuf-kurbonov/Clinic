package uz.muhammadyusuf.kurbonov.myclinic.utils

import android.content.Context
import android.provider.CallLog
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.recievers.CallReceiver.Companion.NOTIFICATION_ID

inline fun <reified T> retries(count: Int, block: () -> T): T {
    var result: T? = null
    var currentIteration = 0
    while (result == null && currentIteration < count) {
        try {
            result = block()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(
                RetriesInternalException(e)
            )
        } finally {
            currentIteration++
        }
    }
    return result ?: throw RetriesExpiredException(count)
}

class RetriesInternalException(e: Exception) : Exception("Error occurred $e", e)

class RetriesExpiredException(retriesCount: Int) :
    Exception("All retries ($retriesCount) are failed")

class PhoneCheckMismatchException(phone1: String, phone2: String) :
    IllegalArgumentException("Invalid phone numbers ${phone1.maskPhoneNumber()} and ${phone2.maskPhoneNumber()}")

fun String.maskPhoneNumber(): String {
    return dropLast(5) + "***" + drop(length - 2)
}

fun getCallDetails(context: Context): CommunicationDataHolder {
    val contacts = CallLog.Calls.CONTENT_URI
    val managedCursor = context.contentResolver.query(
        contacts,
        null,
        null,
        null,
        "date DESC"
    )
    val number = managedCursor!!.getColumnIndex(CallLog.Calls.NUMBER)
    val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
    val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
    var communicationDataHolder: CommunicationDataHolder? = null
    if (managedCursor.moveToFirst()) {
        val phNumber = managedCursor.getString(number)
        val callType = managedCursor.getString(type)
        // long timestamp = convertDateToTimestamp(callDayTime);
        val callDuration = managedCursor.getString(duration)
        val dirCode = callType.toInt()

        communicationDataHolder = CommunicationDataHolder(
            phNumber,
            if ((callDuration.toLong() <= 0) && (dirCode != CallLog.Calls.OUTGOING_TYPE)) "declined" else "accepted",
            type = if (dirCode == CallLog.Calls.OUTGOING_TYPE) "outgoing" else "incoming",
            duration = callDuration.toLong()
        )
    }
    managedCursor.close()
    return communicationDataHolder
        ?: throw IllegalStateException("This call isn't registered by system")
}


fun getBaseNotification(context: Context): NotificationCompat.Builder {
    val view = RemoteViews(context.packageName, R.layout.notification_view)
    return NotificationCompat.Builder(context, "clinic_info")
        .apply {

            setContent(view)

            setSmallIcon(R.drawable.ic_launcher_foreground)

            priority = NotificationCompat.PRIORITY_MAX

            setCustomBigContentView(view)

            setAutoCancel(true)
        }
}

fun notifyNotConnectedNotification(context: Context) {
    val notification = NotificationCompat.Builder(context, "clinic_info")
        .apply {

            setContentText(context.getString(R.string.no_connection))

            setContentTitle(context.getString(R.string.app_name))

            setSmallIcon(R.drawable.ic_launcher_foreground)

            priority = NotificationCompat.PRIORITY_MAX

            setAutoCancel(true)
        }
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification.build())
}