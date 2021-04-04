package uz.muhammadyusuf.kurbonov.myclinic.di

import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.utils.NetworkIOException
import uz.muhammadyusuf.kurbonov.myclinic.utils.RetriesExpiredException
import uz.muhammadyusuf.kurbonov.myclinic.utils.retries
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder
import java.io.IOException


val networkModule = module {
    single<APIService> {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://app.32desk.com:3030/")
            .client(get())
            .build()
            .create(APIService::class.java)
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                val url = it.request().toString()
                Timber.tag("request").d(url)

                val newRequest: Request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${get<String>(named("token"))}")
                    .build()

                try {
                    retries(3) {
                        it.proceed(newRequest)
                    }
                } catch (e: IOException) {
                    FirebaseCrashlytics.getInstance().log("DataHolder is $DataHolder")
                    FirebaseCrashlytics.getInstance().recordException(
                        NetworkIOException(e)
                    )
                    Timber.d(e)
                    errorResponse(newRequest, e)
                } catch (e: RetriesExpiredException) {
                    errorResponse(newRequest, e)
                }
            }
            .build()
    }
}

fun errorResponse(request: Request, e: Exception): Response = Response.Builder()
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
    .code(407)
    .build()