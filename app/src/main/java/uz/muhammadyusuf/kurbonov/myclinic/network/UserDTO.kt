package uz.muhammadyusuf.kurbonov.myclinic.network

import androidx.annotation.Keep

@Keep
data class UserDTO(
    val code: String,
    val item: Item?,
    val status: String
)