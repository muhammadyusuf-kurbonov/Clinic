package uz.muhammadyusuf.kurbonov.myclinic.di

import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    single<SharedPreferences> {
        androidContext().getSharedPreferences("main", 0)
    }

    factory(named("token")) {
        get<SharedPreferences>().getString("token", "")
    }
}