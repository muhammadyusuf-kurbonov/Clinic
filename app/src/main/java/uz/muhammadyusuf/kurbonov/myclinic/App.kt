package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import org.koin.core.context.startKoin
import uz.muhammadyusuf.kurbonov.myclinic.di.networkModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                networkModule
            )
        }
    }
}