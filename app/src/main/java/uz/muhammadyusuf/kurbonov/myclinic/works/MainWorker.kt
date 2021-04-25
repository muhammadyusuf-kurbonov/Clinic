package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.NotificationView
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
) {

    companion object {
        const val WORKER_ID = "main_work"
    }

    private var isActive = true
    private val primaryNotificationID = 100

    private fun deactivateWorker() {
        // must be last
        isActive = false
        log("Deactivated")
    }

    override suspend fun doWork(): Result {

        isActive = true

        val notificationView = NotificationView(applicationContext, App.appViewModel.state)
        notificationView.onFinished = {
            deactivateWorker()
        }
        notificationView.start()

        log("new scope created and launcher")

        // Keep worker live
        while (isActive) {
            // cycle
        }

        log("Work done!")
        return Result.success()
    }

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(primaryNotificationID, getNotificationTemplate().build()).also {
            App.appViewModel.reduceBlocking(Action.Restart)
        }
    }

    private fun getNotificationTemplate() =
        NotificationCompat.Builder(applicationContext, "32desk_notification_channel")
            .apply {

                setChannelId("32desk_notification_channel")

                setContentTitle(applicationContext.getString(R.string.app_name))

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setAutoCancel(true)
            }

    private fun log(msg: String) {
        initTimber()
        Timber.tag("main_worker").d(msg)
    }
}