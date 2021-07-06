package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import android.content.Context
import io.paperdb.Paper
import uz.muhammadyusuf.kurbonov.myclinic.di.AppComponent
import uz.muhammadyusuf.kurbonov.myclinic.di.DaggerAppComponent

class App : Application() {


    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"
    }

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
        Paper.init(this)
    }
}

fun Context.appComponent(): AppComponent {
    return if (this is App) this.appComponent
    else (this.applicationContext as App).appComponent
}