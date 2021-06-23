package uz.muhammadyusuf.kurbonov.myclinic.core.states

import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer

sealed class CustomerState {
    object Default : CustomerState()
    data class Found(val customer: Customer) : CustomerState()
    object NotFound : CustomerState()
    object ConnectionFailed : CustomerState()
}