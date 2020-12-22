package uz.muhammadyusuf.kurbonov.myclinic.network

import retrofit2.http.GET
import retrofit2.http.Query

interface UserSearchService {

    @GET("api/findUser")
    suspend fun searchUser(@Query("search") phone: String): UserDTO

}