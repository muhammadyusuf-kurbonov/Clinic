package uz.muhammadyusuf.kurbonov.myclinic.di

import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import uz.muhammadyusuf.kurbonov.myclinic.android.SystemFunctionsProviderImpl
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import javax.inject.Qualifier
import javax.inject.Singleton

@Component(modules = [AppModule::class])
interface AppComponent {

    @Singleton
    fun appStatesControllerFactory(): AppStatesControllerFactory.Factory
}

@Module(includes = [RepositoryModule::class])
object AppModule

@Module(includes = [SystemFunctionsProviderModule::class])
object RepositoryModule {
    @Provides
    fun provideProdRepository(provider: SystemFunctionsProvider): AppRepository {
        return AppRepository(
            provider.readPreference("token", "")
        )
    }

    @Provides
    @Local
    fun provideLocalRepository(provider: SystemFunctionsProvider): AppRepository {
        return AppRepository(
            provider.readPreference("token", ""),
            "http://10.0.2.2:3030/"
        )
    }
}

@Module
interface SystemFunctionsProviderModule {
    @Binds
    fun bindSystemFunctionProvider(implementation: SystemFunctionsProviderImpl): SystemFunctionsProvider
}

@Qualifier
annotation class Local