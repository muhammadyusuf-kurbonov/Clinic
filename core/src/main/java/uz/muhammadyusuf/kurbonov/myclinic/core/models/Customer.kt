package uz.muhammadyusuf.kurbonov.myclinic.core.models

import java.util.*

data class Customer(
    val id: String,
    val first_name: String,
    val last_name: String,
    val avatar_url: String?,
    val phone: String,
    val lastAppointment: Appointment?,
    val nextAppointment: Appointment?,
) {
    data class Appointment(
        val date: Date,
        val user: String, // doctor
    )
}