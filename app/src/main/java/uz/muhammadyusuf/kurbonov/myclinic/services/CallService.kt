package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.EventBus

class CallReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 155
        const val EXTRA_PHONE = "phone"
    }

    var started = false

    override fun onReceive(context: Context, intent: Intent?) {

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (phoneNumber.isNullOrEmpty())
                return
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val serviceIntent = Intent(context, NotifierService::class.java)
                serviceIntent.putExtra(EXTRA_PHONE, phoneNumber)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                started = true
            } else if (TelephonyManager.EXTRA_STATE_IDLE == phoneState) {
                EventBus.event.value = 1
                NotificationManagerCompat.from(context)
                    .cancel(NOTIFICATION_ID)
            }
        }
    }
}