package uz.muhammadyusuf.kurbonov.myclinic

import android.app.Application
import io.paperdb.Paper

class App : Application() {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"
    }

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }
}