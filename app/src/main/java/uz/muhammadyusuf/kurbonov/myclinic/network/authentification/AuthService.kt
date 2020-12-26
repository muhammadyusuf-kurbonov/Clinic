package uz.muhammadyusuf.kurbonov.myclinic.network.authentification

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("authentication")
    suspend fun authenticate(@Body authRequest: AuthRequest): Response<AuthResponse>
}