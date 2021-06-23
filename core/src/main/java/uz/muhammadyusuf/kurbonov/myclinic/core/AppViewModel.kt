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
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import kotlin.coroutines.CoroutineContext

class AppViewModel(
    parentCoroutineContext: CoroutineContext,
    private val provider: SystemFunctionProvider,
    private val repository: AppRepository
) : CoroutineScope {

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
    private val _authState = MutableStateFlow<AuthState>(AuthState.Default)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Suppress("PropertyName")
    private val _customerState = MutableStateFlow<CustomerState>(CustomerState.Default)
    val customerState: StateFlow<CustomerState> = _customerState.asStateFlow()

    @Suppress("PropertyName")
    private val _reportState = MutableStateFlow<ReportState>(ReportState.Default)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()


    fun handle(action: Action) {
        when (action) {
            is Action.Login -> login(action.username, action.password)
            is Action.Search -> startSearch(action.phone)
            is Action.Report -> sendReport(
                action.isMissed,
                action.callDirection,
                action.duration
            )
            is Action.SetPurpose -> updateReport(action.purpose)
        }
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
            if (username.isEmpty()) {
                _authState.value = AuthState.FieldRequired("username")
                return@launch
            }
            if (password.isEmpty()) {
                _authState.value = AuthState.FieldRequired("password")
                return@launch
            }
            val token: AuthToken = repository.authenticate(
                username,
                password
            )
            provider.writePreference("token", token.token)
            _authState.value = AuthState.AuthSuccess
        } catch (e: AuthRequestException) {
            provider.writePreference("token", "")
            _authState.value = AuthState.AuthRequired
        } catch (e: NotConnectedException) {
            _authState.value = AuthState.ConnectionFailed
        }

    }

    private fun startSearch(phone: String) = launch {
        try {
            val customerDto = repository.search(phone)
            _customerState.value = CustomerState.Found(customerDto.toCustomer())
        } catch (e: CustomerNotFoundException) {
            _customerState.value = CustomerState.NotFound
        } catch (e: AuthRequestException) {
            _customerState.value = CustomerState.Default
            _authState.value = AuthState.AuthRequired
        } catch (e: NotConnectedException) {
            _customerState.value = CustomerState.ConnectionFailed
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
            null, null
        )
    }
}