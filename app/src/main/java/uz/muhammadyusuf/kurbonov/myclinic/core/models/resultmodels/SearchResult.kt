package uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels

import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer

sealed class SearchResult {
    object NotFound : SearchResult()
    object AuthRequested : SearchResult()
    object NoConnection : SearchResult()
    object UnknownError : SearchResult()
    data class Found(val customer: Customer) : SearchResult()
}
