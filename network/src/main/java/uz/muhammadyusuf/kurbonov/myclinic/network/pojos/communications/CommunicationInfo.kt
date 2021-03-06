package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications

import androidx.annotation.Keep

@Keep
data class CommunicationInfo(
    val customerId: String,
    val status: String,
    val duration: Long,
    val type: String,
    val transport: String = "phone",
    val body: String
)