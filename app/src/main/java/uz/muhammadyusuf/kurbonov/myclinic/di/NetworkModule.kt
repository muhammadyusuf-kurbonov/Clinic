package uz.muhammadyusuf.kurbonov.myclinic.di

import okhttp3.OkHttpClient
import org.koin.dsl.module
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.UserSearchService
import uz.muhammadyusuf.kurbonov.myclinic.test.LocalUserService

val networkModule = module {
    single<UserSearchService> {
        LocalUserService()
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