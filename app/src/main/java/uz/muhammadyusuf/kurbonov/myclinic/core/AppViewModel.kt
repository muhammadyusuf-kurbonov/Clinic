package uz.muhammadyusuf.kurbonov.myclinic.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.android.works.MainWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SendConnectionResult
import uz.muhammadyusuf.kurbonov.myclinic.utils.*
import java.util.*

class AppViewModel(private val appRepository: AppRepository) {
    private val _state = MutableStateFlow<State>(State.None)
    val stateFlow: StateFlow<State> = _state.asStateFlow()

    lateinit var callDirection: CallDirection
    lateinit var phone: String

    private lateinit var instance: WorkManager
    private var job = Job()

    // Don't use this scope, because it is observing network until state finished
    private var networkTrackerScope = CoroutineScope(Dispatchers.Default + job)
    private var mainScope = CoroutineScope(Dispatchers.Default + job)

    // scope for views
    val coroutineScope = mainScope

    fun reduce(action: Action) {
        if (action is Action.Start) {
            job = Job()
            networkTrackerScope = CoroutineScope(Dispatchers.Default + job)
            mainScope = CoroutineScope(Dispatchers.Default + job)
        }

        mainScope.launch {
            printToLog("reducing $action in state ${stateFlow.value}")

            when (action) {
                is Action.Search -> {
                    callDirection = action.direction
                    _state.value = State.Searching
                    try {
                        withTimeout(12000) {
                            phone = action.phoneNumber
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
                            sendCallInfo(action.context, (stateFlow.value as State.Found).customer)
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
                    initialize(action.context)
                    _state.value = State.Started
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
            }
            if (!isActive) {
                reduce(Action.Finish)
                cancel()
            }
        }
    }

    private fun addNewCustomerRequest() {
        mainScope.launch {
            @Suppress("SpellCheckingInspection")
            val delay =
                App.pref.getString("autocancel_delay", "-1")?.toLong() ?: -1
            if (delay != -1L) {
                _state.value = State.AddNewCustomerRequest(phone)
                delay(delay)
                reduce(Action.Finish)
            } else {
                reduce(Action.Finish)
            }
        }
    }

    private fun sendCallInfo(context: Context, customer: Customer) {
        mainScope.launch {
            delay(2000)
            val callDetails = getCallDetails(context)

            val status = callDetails.status
            val duration = callDetails.duration

            val result = appRepository.sendCommunicationInfo(
                customer.id,
                status,
                duration,
                callDirection.getAsString()
            )

            handleSendCommunicationResult(customer, result)
            delay(2500)
            reduce(Action.Finish)
        }

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun initialize(context: Context) {
        initTimber()

        initNetworkTracker(context)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        instance = WorkManager.getInstance(context)
        instance.enqueueUniqueWork(
            MainWorker.WORKER_ID,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<MainWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
    }

    private fun initNetworkTracker(context: Context) {
        networkTrackerScope.launch {
            NetworkTracker(context).connectedToInternet.collect {
                Timber.d("Network state received $it")
                if (it && stateFlow.value is State.NoConnectionState)
                    reduce(Action.Restart)
            }
        }
    }

    private fun handleSearchResult(result: SearchResult) {
        _state.value = when (result) {
            SearchResult.AuthRequested -> State.AuthRequest(phone)
            is SearchResult.Found -> State.Found(result.customer, callDirection)
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
        networkTrackerScope.cancel()
        mainScope.cancel()
    }

    private fun printToLog(message: String) {
        Timber.tag(TAG_APP_VIEW_MODEL).d(message)
    }

}