package uz.muhammadyusuf.kurbonov.myclinic.network.communications.response


import androidx.annotation.Keep

@Keep
data class RevenuePercent(
    val measure: String, // %
    val value: Int // 30
)