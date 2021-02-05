package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import uz.muhammadyusuf.kurbonov.myclinic.model.Contact

sealed class SearchStates {
    object Loading : SearchStates()
    class Found(val contact: Contact) : SearchStates()
    class Error(val exception: Exception) : SearchStates()
    object ConnectionError : SearchStates()
    object AuthRequest : SearchStates()
    object NotFound : SearchStates()
}