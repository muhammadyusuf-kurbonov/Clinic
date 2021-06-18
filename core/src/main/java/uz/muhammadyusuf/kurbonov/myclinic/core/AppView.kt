package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AppView(protected val viewModel: AppViewModel) {
    abstract suspend fun onCreate()

    suspend fun start() = withContext(Dispatchers.Main) {
        onCreate()
        viewModel.launch {
            viewModel.stateFlow.collect {
                if (it is State.Finished)
                    finish()
                onStateChange(it)
            }
        }
    }

    abstract suspend fun onStateChange(state: State)

    private fun finish() {
        onFinished()
    }

    open fun onFinished() {}
}