package uz.muhammadyusuf.kurbonov.myclinic

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.android.works.MainWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.*
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import uz.muhammadyusuf.kurbonov.myclinic.shared.recordException
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class App : Application() {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"

        lateinit var pref: SharedPreferences
        internal val actionBus = MutableStateFlow<Action>(Action.None)
        lateinit var callDirection: CallDirection
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        printToConsole = {
            Timber.d(it)
        }

        recordException = {
            Timber.e(it)
        }

        initTimber()

        CoroutineScope(Dispatchers.Default).launch {
            actionBus.collect {
                if (it == Action.Start) {
                    WorkManager.getInstance(this@App).enqueue(
                        OneTimeWorkRequestBuilder<MainWorker>()
                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                            .build()
                    )
                }
                if (it is Action.Search) {
                    callDirection = it.direction
                }
            }
        }

        printToConsole("App is created")
    }
}