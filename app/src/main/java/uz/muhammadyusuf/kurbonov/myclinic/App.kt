package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.android.works.AppRepositoryImpl
import uz.muhammadyusuf.kurbonov.myclinic.android.works.MainWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.di.API
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_APP_LIFECYCLE
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class App : Application() {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"

        lateinit var pref: SharedPreferences
        lateinit var appViewModel: AppViewModel

        fun getAppViewModelInstance(): AppViewModel {
            if (!this::appViewModel.isInitialized) {
                val apiService by lazy {
                    API.getAPIService()
                }

                val appRepository = AppRepositoryImpl(apiService)

                appViewModel = AppViewModel(appRepository)
            }
            return appViewModel
        }
    }

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        initTimber()

        Timber.tag(TAG_APP_LIFECYCLE).d("App is created")

        WorkManager.getInstance(this).cancelUniqueWork(MainWorker.WORKER_ID)

    }


}