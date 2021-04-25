package uz.muhammadyusuf.kurbonov.myclinic.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import uz.muhammadyusuf.kurbonov.myclinic.api.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.api.authentification.AuthResponse
import uz.muhammadyusuf.kurbonov.myclinic.api.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.api.communications.response.CommunicationResponse
import uz.muhammadyusuf.kurbonov.myclinic.api.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.api.customers.CustomerAddRequestBody

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
    ): Response<CommunicationResponse>

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