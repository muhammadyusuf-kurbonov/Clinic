package uz.muhammadyusuf.kurbonov.myclinic.android

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.paperdb.Paper
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import javax.inject.Inject


class SystemFunctionsProviderImpl @Inject constructor() : SystemFunctionsProvider {
    override fun onError(throwable: Throwable): Boolean {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        return true
    }

    override fun writePreference(key: String, value: Any) {
        Paper.book().write(key, value)
    }

    override fun <T> readPreference(key: String, defaultValue: T?): T =
        Paper.book().read(key, defaultValue)
            ?: throw IllegalArgumentException("Default value can\'t be null")

}

fun SystemFunctionsProvider(): SystemFunctionsProvider = SystemFunctionsProviderImpl()