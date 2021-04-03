package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import uz.muhammadyusuf.kurbonov.myclinic.di.coreModule
import uz.muhammadyusuf.kurbonov.myclinic.di.networkModule
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.AppViewModel

class App : Application() {
    companion object {
        lateinit var pref: SharedPreferences
        val appViewModel: AppViewModel by lazy {
            AppViewModel()
        }
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences("main", 0)

        startKoin {
            androidContext(applicationContext)
            modules(
                networkModule,
                coreModule
            )
        }
    }


}