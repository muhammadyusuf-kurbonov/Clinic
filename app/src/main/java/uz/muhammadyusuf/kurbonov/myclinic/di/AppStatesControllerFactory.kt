package uz.muhammadyusuf.kurbonov.myclinic.di

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.SystemFunctionsProvider
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import kotlin.coroutines.CoroutineContext

class AppStatesControllerFactory @AssistedInject constructor(
    @Assisted("CoroutineContext") private val context: CoroutineContext,
    private val repository: AppRepository,
    private val provider: SystemFunctionsProvider
) {

    fun create(): AppStatesController {
        return AppStatesController(context, provider, repository)
    }

    @AssistedFactory
    interface Factory {
        fun factory(@Assisted("CoroutineContext") coroutineContext: CoroutineContext): AppStatesControllerFactory
    }

}