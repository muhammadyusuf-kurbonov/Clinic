package uz.muhammadyusuf.kurbonov.myclinic.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.muhammadyusuf.kurbonov.myclinic.network.models.*
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customers.CustomerAddRequestBody
import uz.muhammadyusuf.kurbonov.myclinic.shared.attempts
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
internal class AppRepositoryImpl(override var token: String, baseUrl: String) : AppRepository {

    private val service by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(getOkHTTPClient())
            .build()
            .create(APIService::class.java)
    }


    fun getOkHTTPClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val headerInterceptor = Interceptor {
            val request = it.request()
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return@Interceptor it.proceed(request)
        }

        val badRequestHandlerInterceptor = Interceptor {
            val response = it.proceed(it.request())
            if (response.code == 400)
                throw IOException(
                    APIException(
                        400,
                        response.message
                    )
                ) // We need IOException to cancel the call
            return@Interceptor response
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(badRequestHandlerInterceptor)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun search(phone: String): CustomerDTO {
        try {
            val customerDTO = attempts(10) {
                service.searchCustomer(phone)
            }
            if (customerDTO.total == 0)
                throw CustomerNotFoundException()
            return customerDTO
        } catch (e: HttpException) {
            if (e.code() == 404)
                throw CustomerNotFoundException()
            else
                throw mapToDomainExceptions(e)
        } catch (e: Exception) {
            throw mapToDomainExceptions(e)
        }

    }

    override suspend fun sendCommunicationInfo(
        customerId: String,
        status: CommunicationStatus,
        duration: Long,
        type: CommunicationType
    ): CommunicationId {
        try {
            val communications = attempts(10) {
                service.registerCommunication(
                    CommunicationInfo(
                        customerId,
                        status.toString(),
                        duration,
                        type.toString(),
                        body = ""
                    )
                )
            }
            return CommunicationId(communications._id)
        } catch (e: Exception) {
            throw mapToDomainExceptions(e)
        }
    }

    override suspend fun updateCommunicationNote(
        communicationId: String,
        note: String
    ) {
        try {
            attempts(10) {
                service.updateCommunicationBody(communicationId, note)
            }
        } catch (e: Exception) {
            throw mapToDomainExceptions(e)
        }
    }

    override suspend fun authenticate(username: String, password: String): AuthToken {
        try {
            val response = attempts(10) {
                service.authenticate(AuthRequest(username, password))
            }
            return AuthToken(response.body()!!.accessToken)
        } catch (e: NullPointerException) {
            // Parsing will throw NPE, if schema is not compatible with model
            throw AuthRequestException()
        } catch (e: Exception) {
            throw mapToDomainExceptions(e)
        }
    }

    override suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        phone: String
    ) {
        try {
            attempts(10) {
                service.addCustomer(
                    CustomerAddRequestBody(
                        firstName,
                        lastName,
                        phone
                    )
                )
            }
        } catch (e: Exception) {
            throw mapToDomainExceptions(e)
        }

    }

}