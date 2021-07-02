package uz.muhammadyusuf.kurbonov.myclinic.android.recievers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import uz.muhammadyusuf.kurbonov.myclinic.android.workers.SearchWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCallReceiver
import java.util.*

class CallReceiver : PhoneCallReceiver() {


    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        if (number != null) {
            WorkManager.getInstance(ctx).enqueueUniqueWork(
                "main",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SearchWorker>()
                    .setInputData(
                        workDataOf(
                            "phone" to number
                        )
                    ).build()
            )
        }
    }

    override fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date) {

    }

    override fun onIncomingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        val duration = end.time - start.time
        AppStatesController.pushAction(
            Action.Report(
                duration / 1000L, CallDirection.INCOMING, duration < 1000
            )
        )
    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        if (number != null) {
            WorkManager.getInstance(ctx).enqueueUniqueWork(
                "main",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SearchWorker>()
                    .setInputData(
                        workDataOf(
                            "phone" to number
                        )
                    ).build()
            )
        }
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date, end: Date) {
        val duration = end.time - start.time
        AppStatesController.pushAction(
            Action.Report(
                duration / 1000L, CallDirection.OUTGOING, duration < 1000
            )
        )
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date) {
        AppStatesController.pushAction(
            Action.Report(
                0L, CallDirection.INCOMING, true
            )
        )
    }
}