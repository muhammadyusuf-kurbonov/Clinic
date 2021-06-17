package uz.muhammadyusuf.kurbonov.myclinic.android.works

import android.content.Context
import android.provider.CallLog
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.views.notification.NotificationView
import uz.muhammadyusuf.kurbonov.myclinic.android.views.overlay.OverlayView
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.SettingsProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.utils.NetworkTracker

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params
), SettingsProvider {
    override suspend fun doWork(): Result {
        coroutineScope {
            val appViewModel = AppViewModel(
                AppRepository(getToken()),
                this@MainWorker
            )

            launch {
                when (getViewType()) {
                    Action.ViewType.NOTIFICATION -> NotificationView(
                        applicationContext,
                        appViewModel
                    )
                    Action.ViewType.OVERLAY -> OverlayView(applicationContext, appViewModel)
                }
            }

            launch {
                App.actionBus.collect {
                    appViewModel.reduce(it)
                }
            }

            launch {
                NetworkTracker(applicationContext).connectedToInternet.distinctUntilChanged()
                    .collect { connected ->
                        if (connected && appViewModel.stateFlow.value is State.NoConnectionState) {
                            appViewModel.reduce(
                                Action.Restart
                            )
                        }
                    }
            }

            launch(Dispatchers.Main) {
                when (getViewType()) {
                    Action.ViewType.NOTIFICATION -> NotificationView(
                        applicationContext,
                        appViewModel
                    ).start()
                    Action.ViewType.OVERLAY -> OverlayView(applicationContext, appViewModel).start()
                }
            }

            appViewModel.reduce(Action.Start)

        }
        return Result.success()
    }

    override fun getToken(): String {
        return App.pref.getString("token", "") ?: ""
    }

    override fun getAutoCancelDelay(): Long {
        return App.pref.getLong("autocancel_delay", -1)
    }

    override fun getLastCallInfo(): CallInfo {
        val contacts = CallLog.Calls.CONTENT_URI
        val managedCursor = applicationContext.contentResolver.query(
            contacts,
            null,
            null,
            null,
            "date DESC"
        )
        val number = managedCursor!!.getColumnIndex(CallLog.Calls.NUMBER)
        val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        var communicationDataHolder: CallInfo? = null
        if (managedCursor.moveToFirst()) {
            val phNumber = managedCursor.getString(number)
            val callType = managedCursor.getString(type)
            val callDuration = managedCursor.getString(duration)
            val dirCode = callType.toInt()

            communicationDataHolder = CallInfo(
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

    override fun getViewType(): Action.ViewType {
        return when (App.pref.getString("interaction_type", "floatingButton")) {
            "floatingButton" -> Action.ViewType.OVERLAY
            "notification" -> Action.ViewType.NOTIFICATION
            else -> throw IllegalArgumentException("No such view type")
        }
    }

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            100,
            NotificationCompat.Builder(applicationContext, App.NOTIFICATION_CHANNEL_ID)
                .apply {

                    setChannelId(App.NOTIFICATION_CHANNEL_ID)

                    setContentTitle(applicationContext.getString(R.string.app_name))

                    setSmallIcon(R.drawable.ic_launcher_foreground)
                }.build()
        ).also {
            App.actionBus.value = Action.Restart
        }
    }
}