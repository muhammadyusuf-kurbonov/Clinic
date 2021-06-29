package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.*
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore.updateAuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore.updateCustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore.updateReportState
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import uz.muhammadyusuf.kurbonov.myclinic.network.*
import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.AppointmentItem
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class AppStatesController(
    parentCoroutineContext: CoroutineContext,
    private val provider: SystemFunctionsProvider,
    private val repository: AppRepository
) : CoroutineScope {

    companion object {
        internal val instances = mutableListOf<AppStatesController>()
        fun pushAction(action: Action) {
            if (instances.isEmpty())
                throw IllegalStateException("No instances are initialized yet")
            println(instances)
            instances.forEach {
                it.handle(action)
                println("$it is handling $action")
            }
        }
    }

    private val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (!provider.onError(throwable))
            coroutineContext.cancel()
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext =
        parentCoroutineContext +
                Dispatchers.Default +
                handler


    init {
        instances.add(this)
        coroutineContext.job.invokeOnCompletion {
            instances.remove(this)
        }
    }

    protected fun finalize() {
        instances.remove(this)
    }

    fun handle(action: Action) {
        when (action) {
            is Action.Login -> login(action.username, action.password)
            Action.Logout -> logout()
            is Action.Search -> startSearch(action.phone)
            is Action.Report -> sendReport(
                action.isMissed,
                action.callDirection,
                action.duration
            )
            is Action.SetPurpose -> updateReport(action.purpose)
            Action.RestoreStates -> restoreFromState()
            Action.SaveStates -> saveStates()
            Action.UpdateToken -> {
                repository.token = provider.readPreference("token", "")
            }
        }
    }

    private fun saveStates() {
        provider.writePreference("authState", AppStateStore.authState.value)
        provider.writePreference("customerState", AppStateStore.customerState.value)
        provider.writePreference("reportState", AppStateStore.reportState.value)
    }

    private fun restoreFromState() {
        updateAuthState(
            provider.readPreference(
                "authState", if (provider.readPreference("token", "").isNotEmpty())
                    AuthState.AuthSuccess else AuthState.AuthRequired
            )
        )
        if (AppStateStore.authState.value is AuthState.AuthSuccess)
            repository.token = provider.readPreference("token", "")

        updateCustomerState(
            provider.readPreference("customerState", CustomerState.Default)
        )
        updateReportState(
            provider.readPreference("reportState", ReportState.Default)
        )
    }

    private fun logout() {
        provider.writePreference("token", "")
        repository.token = ""
        updateAuthState(AuthState.AuthRequired)
    }

    private fun updateReport(purpose: String) = launch {
        if (AppStateStore.reportState.value !is ReportState.PurposeRequested)
            throw IllegalStateException("Can't set purpose without registered communication")
        val communicationId =
            (AppStateStore.reportState.value as ReportState.PurposeRequested).communicationId

        try {
            updateReportState(ReportState.Sending)
            repository.updateCommunicationNote(communicationId, purpose)
            updateReportState(ReportState.Submitted)
        } catch (e: NotConnectedException) {
            updateReportState(
                ReportState.ConnectionFailed
            )
        } catch (e: AuthRequestException) {
            updateAuthState(AuthState.AuthRequired)
            updateReportState(ReportState.Default)
        }
    }

    private fun sendReport(missed: Boolean, callDirection: CallDirection, duration: Long) = launch {
        updateReportState(ReportState.Sending)
        if (AppStateStore.customerState.value is CustomerState.NotFound) {
            updateReportState(ReportState.AskToAddNewCustomer)
            return@launch
        }
        if (AppStateStore.customerState.value is CustomerState.Default)
            throw IllegalStateException("Customer state is default! Call search before sending report")

        if (AppStateStore.customerState.value is CustomerState.ConnectionFailed) {
            updateReportState(ReportState.ConnectionFailed)
            return@launch
        }

        val customer = (AppStateStore.customerState.value as CustomerState.Found).customer
        try {
            if (missed) {
                repository.sendCommunicationInfo(
                    customer.id,
                    CommunicationStatus.MISSED,
                    0,
                    callDirection
                )
                updateReportState(ReportState.Submitted)
            } else {
                val communicationId = repository.sendCommunicationInfo(
                    customer.id,
                    CommunicationStatus.ACCEPTED,
                    duration,
                    callDirection
                )
                updateReportState(
                    ReportState.PurposeRequested(communicationId.id)
                )
            }
        } catch (e: NotConnectedException) {
            updateReportState(
                ReportState.ConnectionFailed
            )
        } catch (e: AuthRequestException) {
            updateAuthState(AuthState.AuthRequired)
            updateReportState(ReportState.Default)
        }
    }

    private fun login(username: String, password: String) = launch {
        try {
            updateAuthState(AuthState.Authenticating)
            val emailValid = username.isNotEmpty() && username.matches(Regex("^(((?! ).)+)@(.+)$"))
            val passwordValid = password.isNotEmpty()
            if (!(emailValid && passwordValid)) {
                updateAuthState(AuthState.ValidationFailed)
                return@launch
            }
            val token: AuthToken = repository.authenticate(
                username,
                password
            )
            provider.writePreference("token", token.token)
            repository.token = token.token
            updateAuthState(AuthState.AuthSuccess)
        } catch (e: AuthRequestException) {
            if (AppStateStore.authState.value == AuthState.Authenticating) {
                provider.writePreference("token", "")
                updateAuthState(AuthState.AuthFailed)
            }
        } catch (e: NotConnectedException) {
            if (AppStateStore.authState.value == AuthState.Authenticating) {
                updateAuthState(AuthState.ConnectionFailed)
            }
        }
    }

    private fun startSearch(phone: String) = launch {
        try {
            updateCustomerState(CustomerState.Searching)
            val customerDto = repository.search(phone)
            val customer = customerDto.toCustomer()
            updateCustomerState(CustomerState.Found(customer))


            val lastAndNextAppointment = customerDto.appointments[0]
            val lastAppointment = lastAndNextAppointment.prev?.let { getAppointment(it) }
            val nextAppointment = lastAndNextAppointment.next?.let { getAppointment(it) }

            updateCustomerState(
                CustomerState.Found(
                    customer.copy(
                        lastAppointment = lastAppointment,
                        nextAppointment = nextAppointment
                    )
                )
            )

        } catch (e: CustomerNotFoundException) {
            updateCustomerState(CustomerState.NotFound)
        } catch (e: AuthRequestException) {
            updateCustomerState(CustomerState.Default)
            updateAuthState(AuthState.AuthRequired)
        } catch (e: NotConnectedException) {
            updateCustomerState(CustomerState.ConnectionFailed)
        }
    }

    private suspend fun getAppointment(appointmentItem: AppointmentItem): Customer.Appointment? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val userLoader = async { repository.getUser(appointmentItem.userId) }
                val treatmentLoader =
                    async { repository.getTreatment(appointmentItem.services[0].treatmentId) }

                val user = userLoader.await()
                val treatment = treatmentLoader.await()

                Customer.Appointment(
                    SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()
                    ).parse(appointmentItem.startAt),
                    "${user.lastName} ${user.firstName}",
                    treatment.label
                )
            } catch (e: Exception) {
                null
            }
        }

    // Mapper method
    private fun CustomerDTO.toCustomer(): Customer {
        if (data.isEmpty())
            throw IllegalArgumentException("Empty data leaked")
        val data = data[0]

        if (appointments.isEmpty())
            throw IllegalArgumentException("No appointments yet?")

        return Customer(
            data._id,
            data.first_name,
            data.last_name,
            data.avatar.url,
            data.phone,
            data.balance,
            null,
            null
        )
    }
}