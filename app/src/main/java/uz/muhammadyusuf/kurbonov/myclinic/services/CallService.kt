package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R


class CallReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PHONE_NUMBER = "phone"
        const val NOTIFICATION_ID = 155
    }

    override fun onReceive(context: Context, intent: Intent?) {

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                if (phoneNumber.isNullOrEmpty())
                    return

                Timber.d("$phoneNumber is calling")
                val notification = NotificationCompat.Builder(context, "clinic_info").apply {

                    val view = RemoteViews(context.packageName, R.layout.toast_view)

                    view.setTextViewText(R.id.tvName, "This is our patient")

                    setSmallIcon(R.drawable.ic_launcher_foreground)

                    setContent(view)

                    priority = NotificationCompat.PRIORITY_MAX
                }.build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)

            } else if (TelephonyManager.EXTRA_STATE_IDLE == phoneState) {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
            }
        }
    }
}






















