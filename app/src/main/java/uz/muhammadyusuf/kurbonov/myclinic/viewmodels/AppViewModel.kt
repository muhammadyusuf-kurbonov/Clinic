package uz.muhammadyusuf.kurbonov.myclinic.viewmodels

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Response
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
import uz.muhammadyusuf.kurbonov.myclinic.utils.startNetworkMonitoring
import uz.muhammadyusuf.kurbonov.myclinic.utils.stopMonitoring
import uz.muhammadyusuf.kurbonov.myclinic.works.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder
import uz.muhammadyusuf.kurbonov.myclinic.works.MainWorker

class AppViewModel {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    private lateinit var callDirection: CallDirection

    private val apiService by inject(APIService::class.java)


    suspend fun reduce(action: Action) {
        when (action) {
            is Action.Search -> {
                _state.value = State.Loading
                log("Searching ${action.phoneNumber}")
                try {
                    withTimeout(12000) {
                        val response = withContext(Dispatchers.IO) {
                            apiService.searchCustomer(DataHolder.phoneNumber, withAppointments = 0)
                        }

                        _state.value = getStateOfResponse(response)
                    }
                } catch (e: Exception) {
                    _state.value = State.Error(e)
                }
            }

            is Action.Finish -> {
                onFinished()
                _state.value = State.Finished
            }


            Action.SetNoConnectionState -> _state.value = State.ConnectionError
            is Action.Start -> initialize(action.context)
            is Action.SetCallDirection -> callDirection = action.callDirection
        }
    }

    fun reduceBlocking(action: Action) {
        runBlocking {
            reduce(action)
        }
    }

    private fun initialize(context: Context) {
        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        //TODO: Remove for release
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "main_work",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<MainWorker>().build()
            )
        startNetworkMonitoring(context)
    }

    private fun getStateOfResponse(response: Response<CustomerDTO>): State {
        return when {
            response.code() == 404 -> State.NotFound
            response.code() == 401 -> State.AuthRequest
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
        stopKoin()
    }

    private fun log(message: String) {
        Timber.d(message)
        FirebaseCrashlytics.getInstance().log(message)
    }
}