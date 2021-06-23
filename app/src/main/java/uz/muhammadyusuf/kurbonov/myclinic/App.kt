package uz.muhammadyusuf.kurbonov.myclinic

import android.annotation.SuppressLint
import android.app.Application

class App : Application() {
    companion object {

        const val NOTIFICATION_CHANNEL_ID = "32desk_notification_channel_low"
        const val HEADUP_NOTIFICATION_CHANNEL_ID = "32desk_notification_channel"
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onCreate() {
        super.onCreate()

    }
}