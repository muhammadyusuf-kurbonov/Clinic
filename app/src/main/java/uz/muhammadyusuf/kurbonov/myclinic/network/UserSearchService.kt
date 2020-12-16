package uz.muhammadyusuf.kurbonov.myclinic.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserSearchService {

    @GET("api/findUser")
    fun searchUser(@Query("search") phone: String): Call<UserDTO>

}