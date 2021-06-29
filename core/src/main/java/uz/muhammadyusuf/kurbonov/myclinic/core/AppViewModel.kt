package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class AppViewModel(
    parentCoroutineContext: CoroutineContext,
    private val provider: SystemFunctionsProvider,
    private val repository: AppRepository
) : CoroutineScope {

    companion object {
        internal val instances = mutableListOf<AppViewModel>()
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


    @Suppress("PropertyName")
    internal val _authState = MutableStateFlow<AuthState>(AuthState.Default)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Suppress("PropertyName")
    internal val _customerState = MutableStateFlow<CustomerState>(CustomerState.Default)
    val customerState: StateFlow<CustomerState> = _customerState.asStateFlow()

    @Suppress("PropertyName")
    internal val _reportState = MutableStateFlow<ReportState>(ReportState.Default)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

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
        }
    }

    fun saveStates() {
        provider.writePreference("authState", authState.value)
        provider.writePreference("customerState", customerState.value)
        provider.writePreference("reportState", reportState.value)
    }

    fun restoreFromState() {
        _authState.value = provider.readPreference("authState", AuthState.Default)
        _customerState.value = provider.readPreference("customerState", CustomerState.Default)
        _reportState.value = provider.readPreference("reportState", ReportState.Default)
    }

    private fun logout() {
        provider.writePreference("token", "")
        repository.token = ""
        _authState.value = AuthState.AuthRequired
    }

    private fun updateReport(purpose: String) = launch {
        if (reportState.value !is ReportState.PurposeRequested)
            throw IllegalStateException("Can't set purpose without registered communication")
        val communicationId = (reportState.value as ReportState.PurposeRequested).communicationId

        try {
            repository.updateCommunicationNote(communicationId, purpose)
            _reportState.value = ReportState.Submitted
        } catch (e: NotConnectedException) {
            _reportState.value = ReportState.ConnectionFailed
        } catch (e: AuthRequestException) {
            _authState.value = AuthState.AuthRequired
            _reportState.value = ReportState.ConnectionFailed
        }
    }

    private fun sendReport(missed: Boolean, callDirection: CallDirection, duration: Long) = launch {
        _reportState.value = ReportState.Sending
        if (customerState.value is CustomerState.NotFound) {
            _reportState.value = ReportState.AskToAddNewCustomer
            return@launch
        }
        if (customerState.value is CustomerState.Default)
            throw IllegalStateException("Customer state is default! Call search before sending report")

        if (customerState.value is CustomerState.ConnectionFailed) {
            _reportState.value = ReportState.ConnectionFailed
            return@launch
        }

        val customer = (customerState.value as CustomerState.Found).customer
        try {
            if (missed) {
                repository.sendCommunicationInfo(
                    customer.id,
                    CommunicationStatus.MISSED,
                    0,
                    callDirection
                )
                _reportState.value = ReportState.Submitted
            } else {
                val communicationId = repository.sendCommunicationInfo(
                    customer.id,
                    CommunicationStatus.ACCEPTED,
                    duration,
                    callDirection
                )
                _reportState.value = ReportState.PurposeRequested(communicationId.id)
            }
        } catch (e: NotConnectedException) {
            _reportState.value = ReportState.ConnectionFailed
        } catch (e: AuthRequestException) {
            _authState.value = AuthState.AuthRequired
            _reportState.value = ReportState.ConnectionFailed
        }
    }

    private fun login(username: String, password: String) = launch {
        try {

            val emailValid = username.isNotEmpty() && username.matches(Regex("^(((?! ).)+)@(.+)$"))
            val passwordValid = password.isNotEmpty()
            if (!(emailValid && passwordValid)) {
                _authState.value = AuthState.ValidationFailed
                return@launch
            }
            _authState.value = AuthState.Authenticating
            val token: AuthToken = repository.authenticate(
                username,
                password
            )
            provider.writePreference("token", token.token)
            repository.token = token.token
            _authState.value = AuthState.AuthSuccess
        } catch (e: AuthRequestException) {
            provider.writePreference("token", "")
            _authState.value = AuthState.AuthFailed
        } catch (e: NotConnectedException) {
            _authState.value = AuthState.ConnectionFailed
        }

    }

    private fun startSearch(phone: String) = launch {
        try {
            val customerDto = repository.search(phone)
            val customer = customerDto.toCustomer()
            _customerState.value = CustomerState.Found(customer)

            val lastAndNextAppointment = customerDto.appointments[0]
            val lastAppointment = lastAndNextAppointment.prev?.let { getAppointment(it) }
            val nextAppointment = lastAndNextAppointment.next?.let { getAppointment(it) }

            _customerState.value = CustomerState.Found(
                customer.copy(
                    lastAppointment = lastAppointment,
                    nextAppointment = nextAppointment
                )
            )

        } catch (e: CustomerNotFoundException) {
            _customerState.value = CustomerState.NotFound
        } catch (e: AuthRequestException) {
            _customerState.value = CustomerState.Default
            _authState.value = AuthState.AuthRequired
        } catch (e: NotConnectedException) {
            _customerState.value = CustomerState.ConnectionFailed
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