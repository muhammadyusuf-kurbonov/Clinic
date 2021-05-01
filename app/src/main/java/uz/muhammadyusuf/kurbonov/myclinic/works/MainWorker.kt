package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.view.OverlayView
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
) {

    companion object {
        const val WORKER_ID = "main_work"
    }

    private val primaryNotificationID = 100


    override suspend fun doWork(): Result {
        val job = CoroutineScope(Dispatchers.Default).launch {
            OverlayView(applicationContext, App.getAppViewModelInstance().state, this)
                .start()
        }
        while (job.isActive) {
            //cycle
        }
        return Result.success()
    }

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            primaryNotificationID,
            NotificationCompat.Builder(applicationContext, "32desk_notification_channel")
                .apply {

                    setChannelId("32desk_notification_channel")

                    setContentTitle(applicationContext.getString(R.string.app_name))

                    setSmallIcon(R.drawable.ic_launcher_foreground)

                    setAutoCancel(true)
                }.build()
        ).also {
            App.getAppViewModelInstance().reduceBlocking(Action.Restart)
        }
    }


    fun printToLog(msg: String) {
        initTimber()
        Timber.tag("main_worker").d(msg)
    }
}