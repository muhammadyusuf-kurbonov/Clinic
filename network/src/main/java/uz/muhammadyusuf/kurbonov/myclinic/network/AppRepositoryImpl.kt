package uz.muhammadyusuf.kurbonov.myclinic.network

import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customers.CustomerAddRequestBody
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.*
import uz.muhammadyusuf.kurbonov.myclinic.shared.attempts
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import uz.muhammadyusuf.kurbonov.myclinic.shared.recordException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
internal class AppRepositoryImpl(val token: String, baseUrl: String) : AppRepository {

    private val service by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(getOkHTTPClient())
            .build()
            .create(APIService::class.java)
    }


    fun getOkHTTPClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val url = it.request().toString()
            printToConsole("Requesting $url")

            val newRequest: Request = it.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            try {
                it.proceed(newRequest)
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                recordException(e)
                errorResponse(newRequest, e, 408)
            } catch (e: ConnectException) {
                e.printStackTrace()
                recordException(e)
                errorResponse(newRequest, e, 408)
            } catch (e: Exception) {
                e.printStackTrace()
                recordException(e)
                errorResponse(newRequest, e, 417)
            }
        }
        .callTimeout(15, TimeUnit.SECONDS)
        .build()


    private fun errorResponse(request: Request, e: Exception, code: Int = 417): okhttp3.Response =
        okhttp3.Response.Builder()
            .request(request)
            .body(
                ResponseBody.create(
                    MediaType.get("application/json"),
                    "{" +
                            "error: $e" +
                            "}"
                )
            )
            .protocol(Protocol.HTTP_1_1)
            .message(e.message ?: "")
            .code(code)
            .build()

    override suspend fun search(phone: String): SearchResult {
        val response = attempts(10) {
            service.searchCustomer(phone)
        }
        return handleSearchResponse(response)
    }

    override suspend fun sendCommunicationInfo(
        customerId: String,
        status: String,
        duration: Long,
        callDirection: String
    ): SendConnectionResult {
        val communications = attempts(10) {
            service.communications(
                CommunicationInfo(
                    customerId,
                    status,
                    duration,
                    callDirection,
                    body = ""
                )
            )
        }


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

    override suspend fun sendCommunicationNote(
        communicationId: String,
        body: String
    ): PatchConnectionResult {
        val response = attempts(10) {
            service.updateCommunicationBody(communicationId, body)
        }
        return if (response.isSuccessful)
            PatchConnectionResult.Success
        else
            PatchConnectionResult.Failed(IllegalStateException(response.raw().toString()))
    }

    override suspend fun authenticate(username: String, password: String): AuthResult {
        val response = attempts(10) {
            service.authenticate(AuthRequest(username, password))
        }
        return when {
            response.isSuccessful -> AuthResult.Success(response.body()!!.accessToken)
            response.code() in arrayListOf(408, 417) -> AuthResult.ConnectionFailed(
                IllegalStateException(response.raw().toString())
            )
            else -> AuthResult.InvalidCredentials
        }
    }

    override suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        phone: String
    ): NewCustomerRequestResult {
        val response = attempts(10) {
            service.addCustomer(
                CustomerAddRequestBody(
                    firstName,
                    lastName,
                    phone
                )
            )
        }
        return when {
            response.isSuccessful -> NewCustomerRequestResult.Success
            else -> NewCustomerRequestResult.Failed(
                IllegalStateException(
                    response.raw().toString()
                )
            )
        }
    }

    private fun handleSearchResponse(response: Response<CustomerDTO>): SearchResult {
        return when {
            response.code() == 404 -> SearchResult.NotFound
            response.code() == 401 -> SearchResult.AuthRequested
            response.code() == 408 -> SearchResult.NoConnection
            response.code() == 417 -> SearchResult.UnknownError
            response.code() == 200 ->
                try {
                    SearchResult.Found(response.body()!!)
                } catch (e: Exception) {
                    e.printStackTrace()
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
        printToConsole("=================Response==============")
        printToConsole("code: ${response.code()}")
        printToConsole("-----------------message---------------")
        printToConsole(response.raw().message())
        printToConsole("--------------end message--------------")
        printToConsole("-----------------body---------------")
        printToConsole(response.raw().body().toString())
        printToConsole("--------------end body--------------")
        printToConsole("-----------------header---------------")
        val headersMap = response.raw().headers().toMultimap()
        headersMap.keys.forEach { key ->
            printToConsole("$key: ${headersMap[key]}")
        }
        printToConsole("--------------end header--------------")

        throw IllegalStateException("Invalid response. See log for more details")
    }

}