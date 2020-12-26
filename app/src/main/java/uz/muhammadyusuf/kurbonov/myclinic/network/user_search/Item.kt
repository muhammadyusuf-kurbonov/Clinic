package uz.muhammadyusuf.kurbonov.myclinic.network.user_search

import androidx.annotation.Keep

@Keep
data class Item(
    val address: Address,
    val company: Company,
    val email: String,
    val id: Int,
    val name: String,
    val phone: String,
    val username: String,
    val website: String
)