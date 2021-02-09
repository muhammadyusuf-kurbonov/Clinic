package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import java.util.*
import java.util.concurrent.TimeUnit


class CallReceiver : PhoneCallReceiver() {

    init {
        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())
    }
    companion object {
        const val NOTIFICATION_ID = 155
        const val EXTRA_PHONE = "phone"
        const val EXTRA_TYPE = "type"

        @JvmField
        @Volatile
        var isSent = false

        @JvmStatic
        @Synchronized
        fun setFlag(sending: Boolean) {
            isSent = sending
            Timber.d("new Flag is $sending")
        }
    }


    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        setFlag(false)


        Timber.d("onIncomingCallReceived $ctx,$number $start")
        if (number.isNullOrEmpty()) {
            return
        }
        startService(ctx, number, "incoming")
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

        sendRequest(
            ctx,
            number!!,
            "accepted",
            "incoming",
            TimeUnit.MILLISECONDS.toSeconds(end.time - start.time)
        )
        ctx.stopService(Intent(ctx, NotifierService::class.java))
    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        setFlag(false)

        if (number.isNullOrEmpty())
            return
        startService(ctx, number, "outgoing")
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (isSent)
            return
        else setFlag(true)

        sendRequest(
            ctx,
            number!!,
            "accepted",
            "outgoing",
            TimeUnit.MILLISECONDS.toSeconds(end.time - start.time)
        )

        ctx.stopService(Intent(ctx, NotifierService::class.java))
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        if (isSent)
            return
        else setFlag(true)
        Timber.d("onMissedCall() called with: ctx = $ctx, number = $number, start = $start")
        sendRequest(ctx, number!!, "declined", "incoming", 0)
        ctx.stopService(Intent(ctx, NotifierService::class.java))
    }

    private fun sendRequest(
        context: Context,
        phone: String,
        status: String,
        type: String,
        duration: Long
    ) {
        val serviceIntent = Intent(context, SenderService::class.java).apply {
            putExtra(
                "data", CommunicationDataHolder(
                    phone, status, type, duration
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Timber.d("Sending ...")
    }

    private fun startService(ctx: Context, number: String?, type: String) {
        val serviceIntent = Intent(ctx, NotifierService::class.java)
        serviceIntent.putExtra(EXTRA_PHONE, number)
        serviceIntent.putExtra(EXTRA_TYPE, type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
    }
}
