package uz.muhammadyusuf.kurbonov.myclinic.api

import com.google.firebase.perf.metrics.AddTrace
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
    @AddTrace(name = "authentication", enabled = true)
    @POST("/authentication")
    suspend fun authenticate(@Body authRequest: AuthRequest): Response<AuthResponse>

    @AddTrace(name = "search_customer", enabled = true)
    @GET("/customers")
    suspend fun searchCustomer(
        @Query("phone") phone: String,
        @Query("withAppointments") withAppointments: Int = 1,
        @Query("noMeta") noMeta: Int = 1
    ): Response<CustomerDTO>

    @AddTrace(name = "send_communications", enabled = true)
    @POST("/communications")
    suspend fun communications(
        @Body communicationInfo: CommunicationInfo
    ): Response<CommunicationResponse>

    @AddTrace(name = "update_communications", enabled = true)
    @PATCH("/communications/{id}")
    @FormUrlEncoded
    suspend fun updateCommunicationBody(
        @Path("id") id: String,
        @Field("body") body: String
    ): Response<ResponseBody>

    @AddTrace(name = "new_customer", enabled = true)
    @POST("/customers")
    suspend fun addCustomer(
        @Body customerAddRequestBody: CustomerAddRequestBody
    ): Response<ResponseBody>
}
