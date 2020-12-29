package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import java.net.SocketTimeoutException

class SendStatusRequest(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val INPUT_PHONE = "work.customer_phone"
        const val INPUT_STATUS = "work.status"
        const val INPUT_DURATION = "work.duration"
        const val INPUT_TYPE = "work.type"
    }

    override fun doWork(): Result {
        return try {
            val customerPhone = inputData.getString(INPUT_PHONE) ?: throw IllegalArgumentException()
            val status = inputData.getString(INPUT_STATUS) ?: throw IllegalArgumentException()
            val duration = inputData.getLong(INPUT_DURATION, 0)
            val type = inputData.getString(INPUT_TYPE) ?: throw IllegalArgumentException()
            val apiService = get(APIService::class.java)
            runBlocking {
                val customerId = apiService.searchCustomer(customerPhone, withAppointments = 0)
                    .body()?.data?.get(0)?._id ?: throw IllegalArgumentException()
                apiService.communications(customerId, status, duration, type)
                Result.success()
            }
        } catch (timeout: SocketTimeoutException) {
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }
}