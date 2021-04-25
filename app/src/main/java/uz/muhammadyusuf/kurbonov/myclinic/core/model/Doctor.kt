package uz.muhammadyusuf.kurbonov.myclinic.core.model

import androidx.annotation.Keep

@Keep
data class Doctor(
    var name: String,
    var avatarLink: String
)