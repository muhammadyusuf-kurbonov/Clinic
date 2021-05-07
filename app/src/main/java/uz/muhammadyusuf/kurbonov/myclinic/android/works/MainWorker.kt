package uz.muhammadyusuf.kurbonov.myclinic.android.works

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.view.NotificationView
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
        try {
            val job =
                CoroutineScope(Dispatchers.Default).launch {
                    when (App.pref.getString("interaction_type", "floatingButton")) {
                        "notification" -> NotificationView(
                            applicationContext,
                            App.getAppViewModelInstance().state,
                            this
                        ).start()
                        else ->
                            OverlayView(
                                applicationContext,
                                App.getAppViewModelInstance().state,
                                this
                            )
                                .start()
                    }
                }
            job.join()
            delay(5000)
        } catch (e: CancellationException) {
        }
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