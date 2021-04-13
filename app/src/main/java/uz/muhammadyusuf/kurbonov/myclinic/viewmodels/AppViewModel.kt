package uz.muhammadyusuf.kurbonov.myclinic.viewmodels

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
import retrofit2.Response
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
import uz.muhammadyusuf.kurbonov.myclinic.utils.getCallDetails
import uz.muhammadyusuf.kurbonov.myclinic.utils.startNetworkMonitoring
import uz.muhammadyusuf.kurbonov.myclinic.utils.stopMonitoring
import uz.muhammadyusuf.kurbonov.myclinic.works.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.works.MainWorker

class AppViewModel(private val apiService: APIService) {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    lateinit var callDirection: CallDirection
    lateinit var phone: String
    private lateinit var instance: WorkManager

    suspend fun reduce(action: Action) {
        when (action) {
            is Action.Search -> {
                _state.value = State.Loading
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
            is Action.Start -> initialize(action.context)
            is Action.SetCallDirection -> callDirection = action.callDirection
            Action.Restart -> reduce(Action.Search(phone))
        }
    }

    private fun sendCallInfo(context: Context, customer: Customer) {

        val callDetails = getCallDetails(context)

        val status = callDetails.status
        val duration = callDetails.duration

        runBlocking {
            val apiService = DI.getAPIService()
            val communications =
                apiService.communications(
                    CommunicationInfo(
                        customer.id,
                        status,
                        duration,
                        callDirection.getAsString(),
                        body = ""
                    )
                )
            if (communications.isSuccessful) {
                _state.value = State.CommunicationInfoSent(
                    customer, communications.body()?._id
                        ?: throw IllegalStateException("communicationId is null")
                )
            } else {
                stopMonitoring()
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
        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        //TODO: Remove for release
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

        instance = WorkManager.getInstance(context)
        instance.enqueueUniqueWork(
            "main_work",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<MainWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        )
        startNetworkMonitoring(context)
    }

    private fun getStateOfResponse(response: Response<CustomerDTO>): State {
        return when {
            response.code() == 404 -> State.NotFound
            response.code() == 401 -> State.AuthRequest(phone)
            response.code() == 407 -> State.ConnectionError
            response.code() == 200 ->
                if (response.body()!!.data.isNotEmpty()) {
                    State.Found(response.body()!!.toContact())
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
        stopMonitoring()
        instance.cancelUniqueWork("main_work")
    }

    private fun log(message: String) {
        Timber.d(message)
        FirebaseCrashlytics.getInstance().log(message)
    }

}