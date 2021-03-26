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
import uz.muhammadyusuf.kurbonov.myclinic.activities.NewUserActivity
import uz.muhammadyusuf.kurbonov.myclinic.recievers.CallReceiver.Companion.NOTIFICATION_ID

class NewUserAskWork(val context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(context, "action_request")
            .apply {
                setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, NewUserActivity::class.java).apply {
                            putExtra("phone", DataHolder.phoneNumber)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
                    )
                )

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setAutoCancel(true)

                setContentText(context.getString(R.string.add_user_request, DataHolder.phoneNumber))
            }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification.build())
        return Result.success()
    }
}