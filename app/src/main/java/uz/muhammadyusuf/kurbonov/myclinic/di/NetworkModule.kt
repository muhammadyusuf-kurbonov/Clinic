package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.user_search.UserSearchService

val networkModule = module {
    single<UserSearchService> {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://app.32desk.com:3030")
            .client(get())
            .build()
            .create(UserSearchService::class.java)
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                Timber.tag("request").d(it.request().toString())
                val newRequest: Request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${get<String>(named("token"))}")
                    .build()
                it.proceed(newRequest)
            }
            .build()
    }
}