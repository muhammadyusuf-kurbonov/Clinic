package uz.muhammadyusuf.kurbonov.myclinic.model

import androidx.annotation.Keep

@Keep
data class Appointment(
    var date: String,
    var doctor: Doctor,
    var diagnosys: String
)