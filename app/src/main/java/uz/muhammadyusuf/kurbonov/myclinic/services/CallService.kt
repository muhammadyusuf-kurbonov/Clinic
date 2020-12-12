package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.MainActivity


class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        Timber.d("Started")

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Timber.d("$phoneNumber is calling you!!!")
                val newIntent = Intent(context, MainActivity::class.java)
                newIntent.putExtra("title", "halloooooo")
                context.startActivity(newIntent)
            }
        }
    }
}