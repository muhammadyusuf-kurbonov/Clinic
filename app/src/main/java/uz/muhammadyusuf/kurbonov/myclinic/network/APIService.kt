package uz.muhammadyusuf.kurbonov.myclinic.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthResponse
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO

interface APIService {
    @POST("/authentication")
    suspend fun authenticate(@Body authRequest: AuthRequest): Response<AuthResponse>

    @GET("/customers")
    suspend fun searchCustomer(
        @Query("phone") phone: String,
        @Query("withAppointments") withAppointments: Int = 1,
        @Query("noMeta") noMeta: Int = 1
    ): Response<CustomerDTO>

    @POST("/communications")
    suspend fun communications(
        @Body communicationInfo: CommunicationInfo
    ): Response<Unit>
}