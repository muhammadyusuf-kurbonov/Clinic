package uz.muhammadyusuf.kurbonov.myclinic.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState

object AppStateStore {
    @Suppress("PropertyName", "ObjectPropertyName")
    private val _authState = MutableStateFlow<AuthState>(AuthState.Default)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    @Suppress("PropertyName", "ObjectPropertyName")
    private val _customerState = MutableStateFlow<CustomerState>(CustomerState.Default)
    val customerState: StateFlow<CustomerState> = _customerState.asStateFlow()

    @Suppress("PropertyName", "ObjectPropertyName")
    private val _reportState = MutableStateFlow<ReportState>(ReportState.Default)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    fun updateAuthState(authState: AuthState) {
        _authState.value = authState
    }

    fun updateCustomerState(customerState: CustomerState) {
        _customerState.value = customerState
    }

    fun updateReportState(reportState: ReportState) {
        _reportState.value = reportState
    }
}