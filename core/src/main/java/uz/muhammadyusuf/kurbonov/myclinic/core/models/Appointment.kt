package uz.muhammadyusuf.kurbonov.myclinic.core.models

import androidx.annotation.Keep

@Keep
data class Appointment(
    var date: String,
    var doctor: Doctor?,
    var diagnosys: String
)