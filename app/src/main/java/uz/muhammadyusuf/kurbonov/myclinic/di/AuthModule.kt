package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthService

val authModule = module {
    factory {
        AuthRequest("demo@32desk.com", "demo")
    }
    single(named("Login-client")) {
        OkHttpClient.Builder()
            .addInterceptor {
                Timber.tag("login").d(it.request().toString())
                it.proceed(it.request())
            }
            .build()
    }
    single<AuthService> {
        Retrofit.Builder()
            .baseUrl("http://app.32desk.com:3030")
            .client(get(named("Login-client")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}