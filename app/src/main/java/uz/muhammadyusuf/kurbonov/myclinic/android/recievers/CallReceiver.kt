package uz.muhammadyusuf.kurbonov.myclinic.android.recievers

import android.content.Context
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber
import java.util.*


class CallReceiver : PhoneCallReceiver() {

    init {
        initTimber()
    }

    companion object {
        @JvmField
        @Volatile
        var isSent = false

        @JvmStatic
        @Synchronized
        fun setFlag(sending: Boolean) {
            isSent = sending
        }
    }


    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        setFlag(false)
        if (number.isNullOrEmpty()) {
            return
        }
        startRecognition(number, "incoming")
    }

    override fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date) {
        Timber.d(
            "onIncomingCallAnswered() called with: ctx = $ctx, number = $number, start = $start"
        )
    }

    override fun onIncomingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (isSent)
            return
        else setFlag(true)

        endCall(
            number
        )
    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        setFlag(false)

        if (number.isNullOrEmpty())
            return
        startRecognition(number, "outgoing")
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (isSent)
            return
        else setFlag(true)

        endCall(
            number
        )
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        if (isSent)
            return
        else setFlag(true)
        endCall(number)
    }

    private fun endCall(
        number: String?
    ) {
        App.actionBus.value = Action.EndCall(
            number ?: throw IllegalArgumentException("null number")
        )

    }

    private fun startRecognition(number: String?, type: String) {
        App.actionBus.value = Action.Start
        App.actionBus.value =
            Action.Search(
                number ?: throw IllegalStateException("No number yet?"),
                CallDirection.parseString(type)
            )

    }
}
