package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("customers")
    suspend fun searchCustomer(
        @Query("phone") phone: String,
        @Query("registerCall") registerCall: Boolean = true
    ): Response<CustomerDTO>

}