package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.AppEvent
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.EventBus
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest.Companion.INPUT_TYPE
import java.util.*

class CallReceiver : PhoneCallReceiver() {

    companion object {
        const val NOTIFICATION_ID = 155
        const val EXTRA_PHONE = "phone"
        const val STATUS = "status"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }

    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        if (number.isNullOrEmpty()) {
            return
        }
        EventBus.event.value = AppEvent.RestoreServiceEvent
        val serviceIntent = Intent(ctx, NotifierService::class.java)
        serviceIntent.putExtra(EXTRA_PHONE, number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
    }

    override fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date) {
        Timber.d("Answered to $number")
    }

    override fun onIncomingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        EventBus.event.value = AppEvent.StopServiceEvent
        NotificationManagerCompat.from(ctx)
            .cancel(NOTIFICATION_ID)
        sendRequest(ctx, number!!, status = "accepted", type = "incoming", end.time - start.time)
    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        if (number.isNullOrEmpty())
            return
        EventBus.event.value = AppEvent.RestoreServiceEvent
        val serviceIntent = Intent(ctx, NotifierService::class.java)
        serviceIntent.putExtra(EXTRA_PHONE, number)
        serviceIntent.putExtra(STATUS, "outgoing")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        EventBus.event.value = AppEvent.StopServiceEvent
        NotificationManagerCompat.from(ctx)
            .cancel(NOTIFICATION_ID)
        sendRequest(ctx, number!!, status = "accepted", type = "outgoing", end.time - start.time)
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        EventBus.event.value = AppEvent.StopServiceEvent
        NotificationManagerCompat.from(ctx)
            .cancel(NOTIFICATION_ID)
        sendRequest(ctx, number!!, "declined", "incoming", 0)
    }

    private fun sendRequest(
        context: Context,
        phone: String,
        status: String,
        type: String,
        duration: Long
    ) {
        val data = Data.Builder()
        data.putString(SendStatusRequest.INPUT_PHONE, phone)
        data.putString(SendStatusRequest.INPUT_STATUS, status)
        data.putLong(SendStatusRequest.INPUT_DURATION, duration)
        data.putString(INPUT_TYPE, type)
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
            .setInputData(data.build())
            .setConstraints(constraint)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}