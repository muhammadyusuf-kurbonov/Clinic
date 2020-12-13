package uz.muhammadyusuf.kurbonov.myclinic.services

//
//class CallReceiver : BroadcastReceiver() {
//
//
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//
//        if (BuildConfig.DEBUG)
//            Timber.plant(Timber.DebugTree())
//
//        if (intent!!.action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
//            val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
//            if (TelephonyManager.EXTRA_STATE_RINGING == phoneState) {
//                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//                val newIntent = Intent(context!!, CallHandlerActivity::class.java)
//                Timber.d("$phoneNumber is calling")
//                newIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
//                newIntent.addFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK or
//                            Intent.FLAG_ACTIVITY_SINGLE_TOP
//                )
//                MainScope().launch {
//                    delay(1000)
//                    context.startActivity(newIntent)
//                }
//            }
//        }
//    }
//}
//
//

import android.telecom.Call
import android.telecom.InCallService
import uz.muhammadyusuf.kurbonov.myclinic.activities.CallHandlerActivity
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.OngoingCall

class CallService : InCallService() {


    override fun onCallAdded(call: Call) {
        OngoingCall.call = call
        CallHandlerActivity.start(this, call.details.handle.schemeSpecificPart)
    }

    override fun onCallRemoved(call: Call) {
        OngoingCall.call = null
    }
}









