package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.api.APIService
import uz.muhammadyusuf.kurbonov.myclinic.utils.NetworkIOException
import uz.muhammadyusuf.kurbonov.myclinic.utils.attempts
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class API {
    companion object {

        private val service by lazy {
            Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://app.32desk.com:3030/")
                .client(getOkHTTPClient())
                .build()
                .create(APIService::class.java)
        }

        fun getAPIService(): APIService {
            return service
        }

        fun getToken(): String {
            return App.pref.getString("token", "") ?: ""
        }

        fun getOkHTTPClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor {
                val url = it.request().toString()
                Timber.tag("request").d(url)

                val newRequest: Request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${getToken()}")
                    .build()

                try {
                    attempts(15) {
                        it.proceed(newRequest)
                    }
                } catch (e: SocketTimeoutException) {
                    e.printStackTrace()
                    Timber.e(NetworkIOException(e))
                    errorResponse(newRequest, e, 408)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.e(e)
                    errorResponse(newRequest, e, 417)
                }
            }
            .callTimeout(15, TimeUnit.SECONDS)
            .build()


        private fun errorResponse(request: Request, e: Exception, code: Int = 417): Response =
            Response.Builder()
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
    }
}