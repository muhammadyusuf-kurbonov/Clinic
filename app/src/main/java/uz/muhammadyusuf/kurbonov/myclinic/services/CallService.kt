package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.activities.CallHandlerActivity


class CallReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PHONE_NUMBER = "phone"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val newIntent = Intent(context!!, CallHandlerActivity::class.java)
                Timber.d("$phoneNumber is calling")
                newIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                newIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                MainScope().launch {
                    delay(1000)
                    context.startActivity(newIntent)
                }
            }
        }
    }
}






















