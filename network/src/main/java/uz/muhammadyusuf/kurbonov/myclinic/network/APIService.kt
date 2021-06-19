package uz.muhammadyusuf.kurbonov.myclinic.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.authentification.AuthResponse
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.response.CommunicationResponse
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customers.CustomerAddRequestBody

interface APIService {
    @POST("/authentication")
    suspend fun authenticate(@Body authRequest: AuthRequest): Response<AuthResponse>

    @GET("/customers")
    suspend fun searchCustomer(
        @Query("phone") phone: String,
        @Query("withAppointments") withAppointments: Int = 1,
        @Query("noMeta") noMeta: Int = 1
    ): CustomerDTO

    @POST("/communications")
    suspend fun registerCommunication(
        @Body communicationInfo: CommunicationInfo
    ): CommunicationResponse

    @PATCH("/communications/{id}")
    @FormUrlEncoded
    suspend fun updateCommunicationBody(
        @Path("id") id: String,
        @Field("body") body: String
    ): Response<ResponseBody>

    @POST("/customers")
    suspend fun addCustomer(
        @Body customerAddRequestBody: CustomerAddRequestBody
    ): Response<ResponseBody>
}