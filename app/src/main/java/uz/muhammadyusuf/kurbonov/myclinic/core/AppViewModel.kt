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
import retrofit2.Response
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.android.works.MainWorker
import uz.muhammadyusuf.kurbonov.myclinic.api.APIService
import uz.muhammadyusuf.kurbonov.myclinic.api.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.api.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.api.toContact
import uz.muhammadyusuf.kurbonov.myclinic.core.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.*
import java.util.*

class AppViewModel(private val apiService: APIService) {
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
                    this@AppViewModel.callDirection = action.direction
                    _state.value = State.Searching
                    printToLog("Searching ${action.phoneNumber}")
                    try {
                        withTimeout(12000) {
                            val response = withContext(Dispatchers.IO) {
                                apiService.searchCustomer(action.phoneNumber, withAppointments = 0)
                            }

                            phone = action.phoneNumber

                            _state.value = getStateOfResponse(response)
                        }
                    } catch (e: TimeoutCancellationException) {
                        _state.value = State.TooSlowConnectionError
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _state.value = State.Error(e)
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
                            mainScope.launch {
                                val delay =
                                    App.pref.getString("autocancel_delay", "-1")?.toLong() ?: -1
                                if (delay != -1L) {
                                    _state.value = State.AddNewCustomerRequest(action.phone)
                                    delay(delay)
                                    reduce(Action.Finish)
                                } else {
                                    reduce(Action.Finish)
                                }
                            }
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
            ensureActive()
        }
    }

    private fun sendCallInfo(context: Context, customer: Customer) {
        mainScope.launch {
            delay(2000)
            val callDetails = getCallDetails(context)

            val status = callDetails.status
            val duration = callDetails.duration

            val apiService = DI.getAPIService()
            val communications =
                retries(10) {
                    apiService.communications(
                        CommunicationInfo(
                            customer.id,
                            status,
                            duration,
                            callDirection.getAsString(),
                            body = ""
                        )
                    )
                }

            if (communications.isSuccessful) {
                if (duration > 0) {
                    _state.value = State.PurposeRequest(
                        customer, communications.body()?._id
                            ?: throw IllegalStateException("communicationId is null")
                    )
                } else
                    reduce(Action.Finish)
            } else {
                if (communications.code() == 407)
                    _state.value = State.NoConnectionState
                else
                    _state.value = State.Error(
                        IllegalStateException(communications.raw().toString())
                    )
                delay(2500)
                reduce(Action.Finish)
            }
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

    private fun getStateOfResponse(response: Response<CustomerDTO>): State {
        return when {
            response.code() == 404 -> State.NotFound
            response.code() == 401 -> State.AuthRequest(phone)
            response.code() == 407 -> State.NoConnectionState
            response.code() == 408 -> State.NoConnectionState
            response.code() == 409 -> State.Error(
                IllegalStateException(
                    response.raw().toString()
                )
            )
            response.code() == 200 ->
                if (response.body()!!.data.isNotEmpty()) {
                    State.Found(response.body()!!.toContact(), callDirection)
                } else {
                    State.NotFound
                }
            else -> {
                printToLog("=================Response==============")
                printToLog("code: ${response.code()}")
                printToLog("-----------------message---------------")
                printToLog(response.raw().message())
                printToLog("--------------end message--------------")
                printToLog("-----------------body---------------")
                printToLog(response.raw().body().toString())
                printToLog("--------------end body--------------")
                printToLog("-----------------header---------------")
                val headersMap = response.raw().headers().toMultimap()
                headersMap.keys.forEach { key ->
                    printToLog("$key: ${headersMap[key]}")
                }
                printToLog("--------------end header--------------")

                throw IllegalStateException("Invalid response. See log for more details")
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