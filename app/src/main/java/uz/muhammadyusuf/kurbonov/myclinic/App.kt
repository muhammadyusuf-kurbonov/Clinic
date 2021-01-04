package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import uz.muhammadyusuf.kurbonov.myclinic.di.coreModule
import uz.muhammadyusuf.kurbonov.myclinic.di.networkModule

class App : Application() {
    companion object {
        val objLock = Any()
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(
                networkModule,
                coreModule
            )
        }
    }
}