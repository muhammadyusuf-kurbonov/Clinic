package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.network.*
import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
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


    fun handle(action: Action) {
        when (action) {
            is Action.Login -> login(action.username, action.password)
            is Action.Search -> startSearch(action.phone)
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