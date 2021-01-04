package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import java.net.SocketException
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
                    .body()?.data?.get(0)?._id
                if (customerId == null) {
                    Timber.d("Unknown user")
                    return@runBlocking Result.success()
                }
                val communications =
                    apiService.communications(CommunicationInfo(customerId, status, duration, type))
                Timber.d("$communications")
                if (communications.isSuccessful)
                    Result.success()
                else
                    Result.failure()
            }
        } catch (timeout: SocketTimeoutException) {
            Result.retry()
        } catch (timeout: SocketException) {
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }
}