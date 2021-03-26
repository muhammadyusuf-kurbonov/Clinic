package uz.muhammadyusuf.kurbonov.myclinic.works

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.recievers.CallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.utils.getCallDetails

class PurposeSelectorWork(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val callDetails = getCallDetails(context)
        val activityIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra(
                "data", callDetails
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }


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
            .notify(CallReceiver.NOTIFICATION_ID, notification)

        return Result.success()
    }
}