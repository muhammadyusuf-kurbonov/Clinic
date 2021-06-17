package uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels

import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO

sealed class SearchResult {
    object NotFound : SearchResult()
    object AuthRequested : SearchResult()
    object NoConnection : SearchResult()
    object UnknownError : SearchResult()
    data class Found(val customer: CustomerDTO) : SearchResult()
}
