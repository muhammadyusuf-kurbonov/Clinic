package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class Stats(
    val arrived: Int, // 1
    val completed: Int, // 1
    val new: Int // 1
)