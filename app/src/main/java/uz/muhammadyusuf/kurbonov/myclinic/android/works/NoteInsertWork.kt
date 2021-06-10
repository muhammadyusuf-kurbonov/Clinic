package uz.muhammadyusuf.kurbonov.myclinic.android.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.di.API
import uz.muhammadyusuf.kurbonov.myclinic.utils.attempts

class NoteInsertWork(context: Context, private val workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val id = workerParams.inputData.getString("id")
            ?: throw IllegalArgumentException("Id wasn't sent")
        val body = workerParams.inputData.getString("body")
            ?: throw IllegalArgumentException("Body wasn't sent")
        val apiService = API.getAPIService()

        return runBlocking {
            val response = attempts(10) {
                apiService.updateCommunicationBody(id, body)
            }
            App.getAppViewModelInstance().reduce(Action.Finish)
            if (response.isSuccessful) {
                Result.success(
                    workDataOf(
                        "id" to id,
                        "body" to body,
                        "response" to response.raw().message()
                    )
                )
            } else {
                Timber.d(response.errorBody()?.string())
                if (runAttemptCount < 10)
                    Result.retry() else
                    Result.failure(
                        workDataOf(
                            "error" to response.errorBody()?.byteStream()?.bufferedReader()
                                ?.readText()
                        )
                    )
            }
        }
    }
}