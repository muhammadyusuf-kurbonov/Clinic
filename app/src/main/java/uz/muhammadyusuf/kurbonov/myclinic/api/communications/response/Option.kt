package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class Option(
    val type: String, // days
    val value: Int // 7
)