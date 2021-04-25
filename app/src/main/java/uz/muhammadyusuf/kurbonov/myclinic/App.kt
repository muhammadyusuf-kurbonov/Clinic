package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import android.content.SharedPreferences
import androidx.work.WorkManager
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_APP_LIFECYCLE
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber
import uz.muhammadyusuf.kurbonov.myclinic.works.MainWorker

class App : Application() {
    companion object {
        lateinit var pref: SharedPreferences
        lateinit var appViewModel: AppViewModel
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences("main", 0)

        initTimber()

        Timber.tag(TAG_APP_LIFECYCLE).d("App is created")

        WorkManager.getInstance(this).cancelUniqueWork(MainWorker.WORKER_ID)

        val apiService by lazy {
            DI.getAPIService()
        }

        appViewModel = AppViewModel(apiService)
    }


}