package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action

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
            App.appViewModel.reduceBlocking(Action.Finish)
            if (response.isSuccessful) {
                Result.success()
            } else {
                Timber.d(response.errorBody()?.string())
                Result.failure(
                    workDataOf(
                        "error" to response.errorBody().toString()
                    )
                )
            }
        }
    }
}