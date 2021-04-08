package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.stopMonitoring

class NoteInsertWork(context: Context, private val workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val id = workerParams.inputData.getString("id")
            ?: throw IllegalArgumentException("Id wasn't sent")
        val body = workerParams.inputData.getString("body")
            ?: throw IllegalArgumentException("Body wasn't sent")
        val apiService = DI.getAPIService()

        return runBlocking {
            val response = apiService.updateCommunicationBody(id, body)
            stopMonitoring()
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        }
    }
}