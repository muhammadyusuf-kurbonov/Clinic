package uz.muhammadyusuf.kurbonov.myclinic.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.collect

abstract class AppNotificationsView(val viewModel: AppViewModel) : LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    abstract suspend fun onStart()

    suspend fun start() {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        savedStateRegistryController.performRestore(null)
        onStart()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        viewModel.stateFlow.collect {
            onStateChange(it)
            if (it is State.Finished)
                finish()
        }
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    abstract suspend fun onStateChange(state: State)

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    override fun getSavedStateRegistry(): SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    fun finish() {
        onFinished()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    abstract fun onFinished()
}