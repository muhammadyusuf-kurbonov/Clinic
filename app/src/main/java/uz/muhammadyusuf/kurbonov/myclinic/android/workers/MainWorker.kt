package uz.muhammadyusuf.kurbonov.myclinic.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
) {
    override suspend fun doWork(): Result {

        return Result.success()
    }
}