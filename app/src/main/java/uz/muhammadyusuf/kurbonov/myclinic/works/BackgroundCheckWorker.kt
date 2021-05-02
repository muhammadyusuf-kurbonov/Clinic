package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import uz.muhammadyusuf.kurbonov.myclinic.App

class BackgroundCheckWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val AUTO_START_PREF_KEY = "autostart_enabled"
    }

    override fun doWork(): Result {
        val pref = App.pref
        val editor = pref.edit()
        editor.putLong(AUTO_START_PREF_KEY, System.currentTimeMillis())
        editor.apply()
        return Result.success()
    }
}