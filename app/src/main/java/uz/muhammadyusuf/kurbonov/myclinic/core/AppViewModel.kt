package uz.muhammadyusuf.kurbonov.myclinic.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Response
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.api.APIService
import uz.muhammadyusuf.kurbonov.myclinic.api.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.api.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.api.toContact
import uz.muhammadyusuf.kurbonov.myclinic.core.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.*
import uz.muhammadyusuf.kurbonov.myclinic.works.MainWorker

class AppViewModel(private val apiService: APIService) {
    private val _state = MutableStateFlow<State>(State.None)
    val state: StateFlow<State> = _state.asStateFlow()

    lateinit var callDirection: CallDirection
    lateinit var phone: String

    private lateinit var instance: WorkManager
    private val job = Job()

    // Don't use this scope, because it is observing network until state finished
    private val networkTrackerScope = CoroutineScope(Dispatchers.Default + job)

    suspend fun reduce(action: Action) {

        if (state.value == State.Finished && action !is Action.Start)
            return

        when (action) {
            is Action.Search -> {
                this.callDirection = action.direction
                _state.value = State.Searching
                log("Searching ${action.phoneNumber}")
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
                onFinished()
                _state.value = State.Finished
            }

            is Action.EndCall -> {
                when (state.value) {
                    is State.Found -> {
                        sendCallInfo(action.context, (state.value as State.Found).customer)
                    }
                    is State.NotFound -> {
                        _state.value = State.AddNewCustomerRequest(action.phone)
                    }
                    else -> {
                        reduce(Action.Finish)
                    }
                }
            }

            Action.SetNoConnectionState -> _state.value = State.ConnectionError


            is Action.Start -> {
                initialize(action.context)
                _state.value = State.Started
            }


            Action.Restart -> if (this::phone.isInitialized) reduce(
                Action.Search(
                    phone,
                    callDirection
                )
            )
        }
    }

    private fun sendCallInfo(context: Context, customer: Customer) {

        val callDetails = getCallDetails(context)

        val status = callDetails.status
        val duration = callDetails.duration

        runBlocking {
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
                if (duration > 0)
                    _state.value = State.PurposeRequest(
                        customer, communications.body()?._id
                            ?: throw IllegalStateException("communicationId is null")
                    )
                else
                    reduce(Action.Finish)
            } else {
                _state.value = State.Error(
                    IllegalStateException(communications.errorBody().toString())
                )
            }
        }
    }

    fun reduceBlocking(action: Action) {
        runBlocking {
            reduce(action)
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
            NetworkTracker(context).connectedToInternet.distinctUntilChanged().collect {
                if (!it) {
                    reduce(Action.SetNoConnectionState)
                } else {
                    reduce(Action.Restart)
                }
            }
        }
    }

    private fun getStateOfResponse(response: Response<CustomerDTO>): State {
        return when {
            response.code() == 404 -> State.NotFound
            response.code() == 401 -> State.AuthRequest(phone)
            response.code() == 407 -> State.ConnectionError
            response.code() == 200 ->
                if (response.body()!!.data.isNotEmpty()) {
                    State.Found(response.body()!!.toContact(), callDirection)
                } else {
                    State.NotFound
                }
            else -> {
                log("=================Response==============")
                log("code: ${response.code()}")
                log("-----------------message---------------")
                log(response.raw().message())
                log("--------------end message--------------")
                log("-----------------body---------------")
                log(response.raw().body().toString())
                log("--------------end body--------------")
                log("-----------------header---------------")
                val headersMap = response.raw().headers().toMultimap()
                headersMap.keys.forEach { key ->
                    log("$key: ${headersMap[key]}")
                }
                log("--------------end header--------------")

                throw IllegalStateException("Invalid response. See log for more details")
            }
        }
    }

    private fun onFinished() {
        FirebaseCrashlytics.getInstance().deleteUnsentReports()
        instance.cancelUniqueWork(MainWorker.WORKER_ID)
        job.cancel()
    }

    private fun log(message: String) {
        Timber.tag(TAG_APP_VIEW_MODEL).d(message)
        FirebaseCrashlytics.getInstance().log(message)
    }

}