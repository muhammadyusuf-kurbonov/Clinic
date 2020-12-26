package uz.muhammadyusuf.kurbonov.myclinic.network.user_search

import retrofit2.http.GET
import retrofit2.http.Query

interface UserSearchService {

    @GET("customers")
    suspend fun searchUser(
        @Query("phone") phone: String,
        @Query("registerCall") registerCall: Boolean = true
    ): UserDTO

}