package uz.muhammadyusuf.kurbonov.myclinic.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.telephony.TelephonyManager
import kotlinx.coroutines.runBlocking
import java.util.*


abstract class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.

        runBlocking {
            if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
                savedNumber = intent.extras!!.getString(EXTRA_PHONE_NUMBER)
            } else {
                val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
                @Suppress("DEPRECATION") val number =
                    intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (number != null)
                    savedNumber = number
                else
                    return@runBlocking
                var state = 0
                when (stateStr) {
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        state = TelephonyManager.CALL_STATE_IDLE
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        state = TelephonyManager.CALL_STATE_OFFHOOK
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        state = TelephonyManager.CALL_STATE_RINGING
                    }
                }
                onCallStateChanged(context, state, savedNumber)

            }
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallReceived(ctx: Context, number: String?, start: Date)
    protected abstract fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date)
    protected abstract fun onIncomingCallEnded(
        ctx: Context,
        number: String?,
        start: Date,
        end: Date
    )

    protected abstract fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date)
    protected abstract fun onOutgoingCallEnded(
        ctx: Context,
        number: String?,
        start: Date,
        end: Date
    )

    protected abstract fun onMissedCall(ctx: Context, number: String?, start: Date)

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    @Synchronized
    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (state == lastState)
            return
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallReceived(context, number, callStartTime!!)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->                 //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                when {
                    lastState != TelephonyManager.CALL_STATE_RINGING -> {
                        isIncoming = false
                        callStartTime = Date()
                        onOutgoingCallStarted(context, savedNumber, callStartTime!!)
                    }
                    else -> {
                        isIncoming = true
                        callStartTime = Date()
                        onIncomingCallAnswered(context, savedNumber, callStartTime!!)
                    }
                }
            TelephonyManager.CALL_STATE_IDLE ->                 //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime!!)
                    }
                    isIncoming -> {
                        onIncomingCallEnded(context, savedNumber, callStartTime!!, Date())
                    }
                    else -> {
                        onOutgoingCallEnded(context, savedNumber, callStartTime!!, Date())
                    }
                }
        }
        lastState = state
    }


    companion object {
        private var callStartTime: Date? = null
        private var isIncoming = false
        private var savedNumber //because the passed incoming is only valid in ringing
                : String? = null

        @Volatile
        private var lastState = TelephonyManager.CALL_STATE_IDLE
    }
}