package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

class CallStateListener(
    val onCallEnded: (String) -> Unit = {}
) : PhoneStateListener() {
    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)
        if (state == TelephonyManager.CALL_STATE_IDLE)
            onCallEnded(phoneNumber ?: "")
    }
}