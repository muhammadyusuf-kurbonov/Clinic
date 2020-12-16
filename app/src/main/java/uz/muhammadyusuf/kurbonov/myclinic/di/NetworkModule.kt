package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.UserSearchService

val networkModule = module {
    single<UserSearchService> {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .baseUrl("https://server-qm.vercel.app/")
            .build().create(UserSearchService::class.java)
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                Timber.tag("request").d(it.request().toString())
                it.proceed(it.request())
            }
            .build()
    }
}