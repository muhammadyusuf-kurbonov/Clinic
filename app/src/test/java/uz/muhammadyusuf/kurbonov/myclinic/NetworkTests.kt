package uz.muhammadyusuf.kurbonov.myclinic

import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.utils.RetriesExpiredException
import uz.muhammadyusuf.kurbonov.myclinic.utils.retries

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class NetworkTests {

    private var token: String = ""
    private lateinit var customer_id: String
    private lateinit var apiService: APIService

    @Before
    fun getAuthData() {
        val okhttp = OkHttpClient.Builder()
            .addInterceptor {
                val url = it.request().toString()
                Timber.tag("request").d(url)
                val newRequest: Request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                try {
                    retries(3) {
                        it.proceed(newRequest)
                    }
                } catch (e: RetriesExpiredException) {
                    Timber.d(e)
                    Response.Builder()
                        .request(newRequest)
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
                        .code(407)
                        .build()
                }
            }
            .build()

        apiService = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://app.32desk.com:3030/")
            .client(okhttp)
            .build()
            .create(APIService::class.java)


        runBlocking {
            val response = apiService.authenticate(
                AuthRequest(
                    email = "demo@32desk.com",
                    password = "demo"
                )
            )
            assert(response.isSuccessful)
            if (response.isSuccessful)
                token = response.body()?.accessToken ?: throw IllegalStateException("No token")

            val customer = apiService.searchCustomer("+998994801416")

            assertEquals(200, customer.code())
            assert(customer.isSuccessful)
            if (customer.isSuccessful)
                customer_id = customer.body()!!.data[0]._id
        }
    }

    @Test
    fun testCommunications() {
        runBlocking {
            val response = apiService.communications(
                CommunicationInfo(
                    customer_id,
                    "accepted",
                    11,
                    "incoming",
                    body = "Test"
                )
            )
            print(response)
            assert(response.isSuccessful)
        }
    }

    @Test
    fun testCommunicationsBodyUpdate() {
        runBlocking {
            val response = apiService.communications(
                CommunicationInfo(
                    customer_id,
                    "accepted",
                    11,
                    "incoming",
                    body = "Test"
                )
            )
            print(response)
            assert(response.isSuccessful)

            val bodyUpdate = apiService.updateCommunicationBody(
                response.body()!!._id,
                "Test success"
            )
            print(bodyUpdate)
            assert(bodyUpdate.isSuccessful)
        }
    }
}