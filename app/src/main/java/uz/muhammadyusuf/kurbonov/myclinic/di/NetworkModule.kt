package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService


val networkModule = module {
    single<APIService> {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://stg.32desk.com:3030/")
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
                it.proceed(newRequest)

            }
            .build()
    }
}