package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest
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
        val serviceIntent = Intent(ctx, NotifierService::class.java)
        serviceIntent.putExtra(EXTRA_PHONE, number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
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
        Timber.d(
            "onIncomingCallEnded() called with: ctx = $ctx, number = $number, start = $start, end = $end"
        )
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
        Timber.d(
            "onOutgoingCallStarted() called with: ctx = $ctx, number = $number, start = $start"
        )
        if (number.isNullOrEmpty())
            return
        val serviceIntent = Intent(ctx, NotifierService::class.java)
        serviceIntent.putExtra(EXTRA_PHONE, number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {

        if (isSent)
            return
        else setFlag(true)
        Timber.d(
            "onOutgoingCallEnded() called with: ctx = $ctx, number = $number, start = $start, end = $end"
        )
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
        Timber.d(
            "sendRequest() called with:\n" +
                    " phone = $phone\n," +
                    " status = $status\n," +
                    " type = $type\n," +
                    " duration = $duration\n" +
                    " on thread ${Thread.currentThread()}"
        )

        val data = Data.Builder()
        data.putString(SendStatusRequest.INPUT_PHONE, phone)
        data.putString(SendStatusRequest.INPUT_STATUS, status)
        data.putLong(
            SendStatusRequest.INPUT_DURATION,
            TimeUnit.MILLISECONDS.toSeconds(duration)
        )
        data.putString(SendStatusRequest.INPUT_TYPE, type)
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
            .setInputData(data.build())
            .setConstraints(constraint)
            .addTag("sender")
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "sender",
                ExistingWorkPolicy.REPLACE,
                request
            )

    }
}
