package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import android.content.SharedPreferences
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.AppViewModel

class App : Application() {
    companion object {
        lateinit var pref: SharedPreferences
        lateinit var appViewModel: AppViewModel
    }

    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences("main", 0)

        val apiService by lazy {
            DI.getAPIService()
        }

        appViewModel = AppViewModel(apiService)
    }
}