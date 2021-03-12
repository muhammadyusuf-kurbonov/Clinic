package uz.muhammadyusuf.kurbonov.myclinic.network.customers

import androidx.annotation.Keep

@Keep
data class CustomerAddRequestBody(
    val userId: String = "",
    val first_name: String,
    val last_name: String,
    val phone: String
)