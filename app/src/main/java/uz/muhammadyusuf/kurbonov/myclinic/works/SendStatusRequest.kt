package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import android.provider.CallLog
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import java.net.SocketException
import java.net.SocketTimeoutException

class SendStatusRequest(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    companion object {
        const val INPUT_NOTE = "work.note"
        const val INPUT_PHONE = "work.customer_phone"
        const val INPUT_STATUS = "work.status"
        const val INPUT_DURATION = "work.duration"
        const val INPUT_TYPE = "work.type"
    }

    override fun doWork(): Result {
        return try {
            val customerPhone = inputData.getString(INPUT_PHONE) ?: throw IllegalArgumentException()
            val status = inputData.getString(INPUT_STATUS) ?: throw IllegalArgumentException()
            var duration = inputData.getLong(INPUT_DURATION, 0)
            val type = inputData.getString(INPUT_TYPE) ?: throw IllegalArgumentException()
            val note = inputData.getString(INPUT_NOTE) ?: ""

            val apiService = get(APIService::class.java)
            runBlocking {
                val searchCustomer =
                    apiService.searchCustomer(customerPhone, withAppointments = 0)

                if (searchCustomer.code() == 404)
                    return@runBlocking Result.success()

                if (searchCustomer.code() == 401)
                    return@runBlocking Result.retry()

                if (!searchCustomer.isSuccessful) {
                    return@runBlocking Result.failure(
                        Data.Builder().putString("error", searchCustomer.raw().message()).build()
                    )
                }

                if (searchCustomer.body()?.data.isNullOrEmpty())
                    return@runBlocking Result.success()

                val customerId = searchCustomer
                    .body()?.data?.get(0)?._id
                if (customerId == null) {
                    Timber.d("Unknown user")
                    return@runBlocking Result.success()
                }
                if (type == "outgoing") {
                    duration = getLastCallDuration(customerPhone).toLong()
                }
                val communications =
                    apiService.communications(
                        CommunicationInfo(
                            customerId,
                            status,
                            duration,
                            type,
                            note = note
                        )
                    )
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

    private fun getLastCallDuration(phoneNumber: String): String {
        var duration = "0"
        val contacts = CallLog.Calls.CONTENT_URI// Device call log table root
        // Get device call log sqLite table
        val callLogCursor = appContext.contentResolver.query(
            contacts,
            null,
            CallLog.Calls.NUMBER + " = ?",
            arrayOf(phoneNumber),
            CallLog.Calls.DATE + " DESC"
        ) // Sorting the results so that the last call be first we get.
        val duration1 = callLogCursor!!.getColumnIndex(CallLog.Calls.DURATION)
        if (callLogCursor.moveToFirst()) {
            duration =
                callLogCursor.getString(duration1) // Getting the actual duration from your device.
        }
        callLogCursor.close()
        return duration
    }
}