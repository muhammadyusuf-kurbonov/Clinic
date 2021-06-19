package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.muhammadyusuf.kurbonov.myclinic.core.login.LoginActions
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import kotlin.coroutines.CoroutineContext

open class AppViewModel<AT, ST>(
    parentCoroutineContext: CoroutineContext,
    protected val provider: SystemFunctionProvider,
    protected val repository: AppRepository
) : CoroutineScope {

    private val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (!provider.onError(throwable))
            coroutineContext.cancel()
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext =
        parentCoroutineContext +
                Dispatchers.Default +
                handler

    @Suppress("PropertyName")
    protected val _state = MutableStateFlow<ST?>(null)
    val state: StateFlow<ST?> = _state.asStateFlow()

    open fun handle(loginAction: LoginActions) {

    }
}