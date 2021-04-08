package uz.muhammadyusuf.kurbonov.myclinic.viewmodels

import uz.muhammadyusuf.kurbonov.myclinic.model.Customer

sealed class State {
    object Loading : State()
    object Finished : State()

    class Found(val customer: Customer) : State()
    object NotFound : State()

    class AddNewCustomerRequest(val phone: String) : State()
    object AuthRequest : State()

    class Error(val exception: Exception) : State()
    object ConnectionError : State()
}