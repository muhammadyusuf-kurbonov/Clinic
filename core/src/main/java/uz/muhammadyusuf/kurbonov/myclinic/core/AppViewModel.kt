package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.models.LifecycleEvents
import uz.muhammadyusuf.kurbonov.myclinic.core.utils.toContact
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.PatchConnectionResult
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.SendConnectionResult
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import uz.muhammadyusuf.kurbonov.myclinic.shared.recordException
import java.util.*
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    private val appRepository: AppRepository,
    private val settingsProvider: SettingsProvider,
    context: CoroutineContext
) : CoroutineScope {
    private val _state = MutableStateFlow<State>(State.None)
    val stateFlow: StateFlow<State> = _state.asStateFlow()

    lateinit var callDirection: CallDirection
    lateinit var phone: String

    private val handler = CoroutineExceptionHandler { _, throwable ->
        recordException(throwable)
    }

    var lifecycleObserver: (LifecycleEvents) -> Unit = {}

    fun reduce(action: Action) {
        printToConsole("reducing $action in state ${stateFlow.value}")

        when (action) {
            is Action.Search -> {
                callDirection = action.direction
                _state.value = State.Searching
                try {
                    phone = action.phoneNumber
                    launch {
                        val response = appRepository.search(action.phoneNumber)
                        handleSearchResult(response)
                    }
                } catch (e: TimeoutCancellationException) {
                    _state.value = State.ConnectionTimeoutState
                }
            }

            is Action.Finish -> {
                _state.value = State.Finished
                onFinished()
            }

            is Action.EndCall -> {
                when (stateFlow.value) {
                    is State.Found -> {
                        sendCallInfo((stateFlow.value as State.Found).customer)
                    }
                    is State.NotFound -> {
                        addNewCustomerRequest()
                    }
                    else -> {
                        reduce(Action.Finish)
                    }
                }
            }

            is Action.Start -> {
                initialize()
                _state.value = State.Started
                lifecycleObserver(LifecycleEvents.Started)
            }

            Action.Restart -> if (this@AppViewModel::phone.isInitialized) {
                reduce(
                    Action.Search(
                        phone,
                        callDirection
                    )
                )
            }

            Action.SetNoConnectionState -> {
                if (_state.value is State.Searching)
                    _state.value = State.NoConnectionState
            }
            Action.None -> {
            }
            is Action.SendCommunicationNote -> {
                launch {
                    val result = appRepository.sendCommunicationNote(
                        action.communicationId,
                        action.body
                    )
                    if (result is PatchConnectionResult.Failed)
                        result.exception.printStackTrace()
                    reduce(Action.Finish)
                }
            }
        }
    }

    private fun addNewCustomerRequest() {
        launch {
            @Suppress("SpellCheckingInspection")
            val delay = settingsProvider.getAutoCancelDelay()
            if (delay != -1L) {
                _state.value = State.AddNewCustomerRequest(phone)
                delay(delay)
                reduce(Action.Finish)
            } else {
                reduce(Action.Finish)
            }
        }
    }

    private fun sendCallInfo(customer: Customer) {
        launch {
            delay(2000)
            val callDetails = settingsProvider.getLastCallInfo()

            val status = callDetails.status
            val duration = callDetails.duration

            val result = appRepository.sendCommunicationInfo(
                customer.id,
                status,
                duration,
                callDirection.getAsString()
            )

            handleSendCommunicationResult(customer, result)
        }

    }

    private fun initialize() {
        lifecycleObserver(LifecycleEvents.Initialized)
    }

    private fun handleSearchResult(result: SearchResult) {
        _state.value = when (result) {
            SearchResult.AuthRequested -> State.AuthRequest(phone)
            is SearchResult.Found -> State.Found(result.customer.toContact(), callDirection)
            SearchResult.NoConnection -> State.NoConnectionState
            SearchResult.NotFound -> State.NotFound
            SearchResult.UnknownError -> State.Error(IllegalStateException("Error occurred. See logs for details"))
        }
    }

    private fun handleSendCommunicationResult(customer: Customer, result: SendConnectionResult) {
        _state.value = when (result) {
            is SendConnectionResult.Failed -> State.Error(result.exception)
            SendConnectionResult.NotConnected -> State.NoConnectionState
            is SendConnectionResult.PurposeRequest -> {
                State.PurposeRequest(
                    customer,
                    result.communicationId
                )
            }
            SendConnectionResult.Success -> {
                reduce(Action.Finish)
                State.Finished
            }
        }
    }

    private fun onFinished() {
        lifecycleObserver(LifecycleEvents.Finished)
        cancel()
    }

    override val coroutineContext: CoroutineContext = context + SupervisorJob() + handler
}