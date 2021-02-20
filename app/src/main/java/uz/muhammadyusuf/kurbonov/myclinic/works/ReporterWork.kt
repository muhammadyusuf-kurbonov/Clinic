package uz.muhammadyusuf.kurbonov.myclinic.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.runBlocking
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.NOTIFICATION_ID
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

class ReporterWork(val context: Context, private val workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val INPUT_STATUS = "input.status"
        const val INPUT_DURATION = "input.duration"
    }

    override fun doWork(): Result {
        val status = workerParams.inputData.getString(INPUT_STATUS)
        val duration = workerParams.inputData.getLong(INPUT_DURATION, 0)
        val activityIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra(
                "data", CommunicationDataHolder(
                    DataHolder.phoneNumber,
                    status ?: throw IllegalStateException("state is null???"),
                    if (DataHolder.type == CallTypes.INCOME) "incoming" else "outgoing",
                    duration
                )
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        NotificationManagerCompat.from(context).cancelAll()

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

        if (DataHolder.searchState !is SearchStates.Found || DataHolder.phoneNumber.isEmpty()) return Result.success()

        if (DataHolder.type == null) return Result.failure()
        runBlocking {

            if (status == "declined") {
                NotificationManagerCompat.from(context)
                    .cancelAll()
                val data = Data.Builder()
                data.putString(SendStatusRequest.INPUT_PHONE, DataHolder.phoneNumber)
                data.putString(SendStatusRequest.INPUT_STATUS, status)
                data.putLong(
                    SendStatusRequest.INPUT_DURATION,
                    duration
                )
                data.putString(SendStatusRequest.INPUT_NOTE, "")
                data.putString(
                    SendStatusRequest.INPUT_TYPE,
                    if (DataHolder.type == CallTypes.INCOME) "incoming" else "outgoing"
                )
                val constraint = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
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