package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.response


import androidx.annotation.Keep

@Keep
data class TreatmentsPercent(
    val _id: String, // 5e294ada4d6426660f2567cc
    val measure: String, // %
    val treatmentId: String, // 5e13298c102502681992e30b
    val value: Int // 15
)