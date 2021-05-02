package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.view.OverlayView

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
        delay(5000)
        return Result.success()
    }

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            primaryNotificationID,
            NotificationCompat.Builder(applicationContext, App.NOTIFICATION_CHANNEL_ID)
                .apply {

                    setChannelId(App.NOTIFICATION_CHANNEL_ID)

                    setContentTitle(applicationContext.getString(R.string.app_name))

                    setSmallIcon(R.drawable.ic_launcher_foreground)
                }.build()
        ).also {
            App.getAppViewModelInstance().reduceBlocking(Action.Restart)
        }
    }
}