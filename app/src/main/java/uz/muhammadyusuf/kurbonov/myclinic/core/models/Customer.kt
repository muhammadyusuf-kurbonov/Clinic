package uz.muhammadyusuf.kurbonov.myclinic.core.models

import androidx.annotation.Keep

@Keep
data class Customer(
    var id: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var balance: Long = 0,
    var avatarLink: String? = null,
    var lastVisit: String = "",
    var lastAppointment: Appointment? = null,
    var nextAppointment: Appointment? = null
)