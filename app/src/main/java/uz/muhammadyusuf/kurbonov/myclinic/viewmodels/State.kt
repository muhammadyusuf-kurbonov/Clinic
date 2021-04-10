package uz.muhammadyusuf.kurbonov.myclinic.viewmodels

import uz.muhammadyusuf.kurbonov.myclinic.model.Customer

sealed class State {
    object Loading : State()
    object Finished : State()

    data class Found(val customer: Customer) : State()
    object NotFound : State()

    data class AddNewCustomerRequest(val phone: String) : State()
    data class AuthRequest(val phone: String) : State()
    data class CommunicationInfoSent(val customer: Customer, val _id: String) : State()

    data class Error(val exception: Exception) : State()
    object ConnectionError : State()
    object TooSlowConnectionError : State()
}