package uz.muhammadyusuf.kurbonov.myclinic.core

interface SystemFunctionProvider {
    fun onError(throwable: Throwable): Boolean

    fun writePreference(key: String, value: Any)

    fun <T> readPreference(key: String, defaultValue: T?): T
}