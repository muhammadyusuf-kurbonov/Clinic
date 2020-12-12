package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import uz.muhammadyusuf.kurbonov.myclinic.MainActivity


class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                val newIntent = Intent(context!!, MainActivity::class.java)
                newIntent.putExtra("title", phoneNumber)
                context.startActivity(newIntent)
            }
        }
    }
}






















