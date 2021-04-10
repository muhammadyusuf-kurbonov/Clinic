package uz.muhammadyusuf.kurbonov.myclinic.recievers

import android.content.Context
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.works.CallDirection
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
        startRecognition(ctx, number, "incoming")
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
            ctx, number
        )
    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        setFlag(false)

        if (number.isNullOrEmpty())
            return
        startRecognition(ctx, number, "outgoing")
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        if (isSent)
            return
        else setFlag(true)

        endCall(
            ctx, number
        )
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        if (isSent)
            return
        else setFlag(true)
        endCall(ctx, number)
    }

    private fun endCall(
        context: Context,
        number: String?
    ) {
//        WorkManager.getInstance(context).enqueueUniqueWork(
//            "reporter",
//            ExistingWorkPolicy.REPLACE,
//            OneTimeWorkRequestBuilder<ReporterWork>().build()
//        )
        App.appViewModel.reduceBlocking(
            Action.EndCall(
                context,
                number ?: throw IllegalArgumentException("null number")
            )
        )
    }

    private fun startRecognition(ctx: Context, number: String?, type: String) {
        App.appViewModel.reduceBlocking(Action.Start(ctx))
        App.appViewModel.reduceBlocking(Action.SetCallDirection(CallDirection.parseString(type)))
        App.appViewModel.reduceBlocking(
            Action.Search(
                number ?: throw IllegalStateException("No number yet?")
            )
        )
    }
}
