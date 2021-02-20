package uz.muhammadyusuf.kurbonov.myclinic.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CallLog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.NOTIFICATION_ID
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates
import java.util.*

class ReporterWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private fun getCallDetails(): CommunicationDataHolder {
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

    override fun doWork(): Result {

        Thread.sleep(1500)

        val callDetails = getCallDetails()
        Timber.d("$callDetails")

        val status = callDetails.status
        val duration = callDetails.duration
        val activityIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra(
                "data", callDetails
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        NotificationManagerCompat.from(context).cancelAll()

        if (DataHolder.searchState !is SearchStates.Found)
            return Result.success()

        if (DataHolder.type == CallTypes.OUTCOME && duration == 0L)
            return Result.success()

        if (DataHolder.searchState !is SearchStates.Found || DataHolder.phoneNumber.isEmpty()) return Result.success()

        val notification = NotificationCompat.Builder(context, "clinic_info")
            .apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setOngoing(true)
                setContentText(context.getString(R.string.purpose_msg))
                setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        activityIntent,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
                    )
                )
                setAutoCancel(true)
            }.build()
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, notification)


        if (DataHolder.type == null) return Result.failure()
        runBlocking {

            if (status == "declined") {
                NotificationManagerCompat.from(context)
                    .cancelAll()
                val data = Data.Builder()
                data.putString(SendReportWork.INPUT_PHONE, DataHolder.phoneNumber)
                data.putString(SendReportWork.INPUT_STATUS, status)
                data.putLong(
                    SendReportWork.INPUT_DURATION,
                    duration
                )
                data.putString(SendReportWork.INPUT_NOTE, "")
                data.putString(
                    SendReportWork.INPUT_TYPE,
                    if (DataHolder.type == CallTypes.INCOME) "incoming" else "outgoing"
                )
                val constraint = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<SendReportWork>()
                    .setInputData(data.build())
                    .setConstraints(constraint)
                    .addTag("sender")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "sender",
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                return@runBlocking
            } else
                context.startActivity(activityIntent)
        }
        return Result.success()
    }
}