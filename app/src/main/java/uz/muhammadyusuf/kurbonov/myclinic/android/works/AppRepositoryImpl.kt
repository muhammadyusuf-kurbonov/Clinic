package uz.muhammadyusuf.kurbonov.myclinic.android.works

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.api.APIService
import uz.muhammadyusuf.kurbonov.myclinic.api.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.api.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.api.toContact
import uz.muhammadyusuf.kurbonov.myclinic.core.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SendConnectionResult
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class AppRepositoryImpl(private val apiService: APIService) : AppRepository {
    override suspend fun search(phone: String) = withContext(Dispatchers.IO) {
        val response = apiService.searchCustomer(phone, withAppointments = 0)
        handleSearchResponse(response)
    }

    private fun handleSearchResponse(response: Response<CustomerDTO>): SearchResult {
        return when {
            response.code() == 404 -> SearchResult.NotFound
            response.code() == 401 -> SearchResult.AuthRequested
            response.code() == 408 -> SearchResult.NoConnection
            response.code() == 417 -> SearchResult.UnknownError
            response.code() == 200 ->
                try {
                    SearchResult.Found(response.body()!!.toContact())
                } catch (e: Exception) {
                    printError(response)
                    SearchResult.UnknownError
                }
            else -> {
                printError(response)
                SearchResult.UnknownError
            }
        }
    }

    private fun printError(response: Response<CustomerDTO>) {
        printToLog("=================Response==============")
        printToLog("code: ${response.code()}")
        printToLog("-----------------message---------------")
        printToLog(response.raw().message())
        printToLog("--------------end message--------------")
        printToLog("-----------------body---------------")
        printToLog(response.raw().body().toString())
        printToLog("--------------end body--------------")
        printToLog("-----------------header---------------")
        val headersMap = response.raw().headers().toMultimap()
        headersMap.keys.forEach { key ->
            printToLog("$key: ${headersMap[key]}")
        }
        printToLog("--------------end header--------------")

        throw IllegalStateException("Invalid response. See log for more details")
    }

    private fun printToLog(message: String) {
        initTimber()
        Timber.tag("repository").d(message)
    }

    override suspend fun sendCommunicationInfo(
        customerId: String,
        status: String,
        duration: Long,
        callDirection: String
    ): SendConnectionResult {
        val communications = apiService.communications(
            CommunicationInfo(
                customerId,
                status,
                duration,
                callDirection,
                body = ""
            )
        )


        @Suppress("LiftReturnOrAssignment")
        if (communications.isSuccessful) {
            return if (duration > 0) {
                SendConnectionResult.PurposeRequest(communications.body()!!._id)
            } else {
                SendConnectionResult.Success
            }
        } else {
            return if (communications.code() == 408)
                SendConnectionResult.NotConnected
            else
                SendConnectionResult.Failed(
                    IllegalStateException(communications.raw().toString())
                )
        }
    }

}