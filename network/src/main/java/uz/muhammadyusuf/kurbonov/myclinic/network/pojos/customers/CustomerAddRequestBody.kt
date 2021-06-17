package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customers

import androidx.annotation.Keep

@Keep
data class CustomerAddRequestBody(
    val first_name: String,
    val last_name: String,
    val phone: String
)