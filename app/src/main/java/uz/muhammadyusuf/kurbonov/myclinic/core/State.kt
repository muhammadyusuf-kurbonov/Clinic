package uz.muhammadyusuf.kurbonov.myclinic.core

import uz.muhammadyusuf.kurbonov.myclinic.core.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection

sealed class State {
    object None : State()
    object Started : State()
    object Finished : State()

    object Searching : State()
    data class Found(val customer: Customer, val callDirection: CallDirection) : State()
    object NotFound : State()

    data class AddNewCustomerRequest(val phone: String) : State()
    data class AuthRequest(val phone: String) : State()
    data class PurposeRequest(val customer: Customer, val communicationId: String) : State()

    data class Error(val exception: Exception) : State()
    object ConnectionError : State()
    object TooSlowConnectionError : State()
}