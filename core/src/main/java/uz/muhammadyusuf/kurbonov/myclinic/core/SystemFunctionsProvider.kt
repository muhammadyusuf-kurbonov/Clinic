package uz.muhammadyusuf.kurbonov.myclinic.core

interface SystemFunctionsProvider {
    /**
     * This method is for handling error (send to crashlitics services for example)
     *
     * @return boolean true if this error was handled finally and app shouldn't close;
     *          false if error is critical and app will crash
     */
    fun onError(throwable: Throwable): Boolean

    fun writePreference(key: String, value: Any)

    fun <T> readPreference(key: String, defaultValue: T?): T
}