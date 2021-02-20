package uz.muhammadyusuf.kurbonov.myclinic.services

import android.content.Context
import androidx.work.*
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.works.*
import uz.muhammadyusuf.kurbonov.myclinic.works.EnterWork.Companion.INPUT_TYPE
import java.util.*


class CallReceiver : PhoneCallReceiver() {

    init {
        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())
    }
    companion object {
        const val NOTIFICATION_ID = 155

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
            ctx
        )
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
            ctx
        )
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        if (isSent)
            return
        else setFlag(true)
        sendRequest(ctx)
    }

    private fun sendRequest(
        context: Context
    ) {
        val workerRequest = OneTimeWorkRequestBuilder<ReporterWork>()

        workerRequest.build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reporter", ExistingWorkPolicy.REPLACE, workerRequest.build()
        )
    }

    private fun startService(ctx: Context, number: String?, type: String) {
        val enterWorker = OneTimeWorkRequestBuilder<EnterWork>()

        enterWorker.setInputData(Data.Builder().apply {
            putString(EnterWork.INPUT_PHONE, number)
            DataHolder.phoneNumber = number ?: ""
            putString(INPUT_TYPE, type)
        }.build())

        WorkManager.getInstance(ctx).beginWith(
            enterWorker.build()
        ).then(OneTimeWorkRequest.from(SearchWork::class.java))
            .then(OneTimeWorkRequest.from(NotifierWorker::class.java))
            .enqueue()
    }
}
