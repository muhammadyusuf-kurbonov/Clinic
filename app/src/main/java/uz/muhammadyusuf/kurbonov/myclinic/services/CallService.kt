package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.telephony.TelephonyManager
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.Bus
import uz.muhammadyusuf.kurbonov.myclinic.activities.CallHandlerActivity


class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val newIntent = Intent(context!!, CallHandlerActivity::class.java)
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Timber.d("$phoneNumber is calling")
                newIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                newIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                context.startActivity(newIntent)
            }
            Bus.state.value = phoneState ?: ""
        }
    }
}










