package uz.muhammadyusuf.kurbonov.myclinic.android.views

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import uz.muhammadyusuf.kurbonov.myclinic.core.AppView
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel

abstract class BaseView(viewModel: AppViewModel) :
    AppView(viewModel), LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override suspend fun onCreate() {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    open suspend fun onResume() {}

    open suspend fun onStart() {}

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
    override fun getSavedStateRegistry(): SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    override fun onFinished() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}