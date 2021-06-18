package uz.muhammadyusuf.kurbonov.myclinic

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineExceptionHandler
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
import uz.muhammadyusuf.kurbonov.myclinic.utils.FileDebugTree

class App : Application() {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"

        internal val actionBus = MutableStateFlow<Action>(Action.None)
        lateinit var pref: SharedPreferences
        lateinit var callDirection: CallDirection


    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        recordException(throwable)
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

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(FileDebugTree())

        CoroutineScope(Dispatchers.Default + handler).launch {
            actionBus.collect {
                if (it == Action.Start) {
                    WorkManager.getInstance(this@App).enqueueUniqueWork(
                        "main_worker",
                        ExistingWorkPolicy.KEEP,
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