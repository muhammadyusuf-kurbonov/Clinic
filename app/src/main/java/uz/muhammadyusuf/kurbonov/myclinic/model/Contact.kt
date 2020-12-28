package uz.muhammadyusuf.kurbonov.myclinic.model

import androidx.annotation.Keep

@Keep
data class Contact(
    var id: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var balance: Long = 0,
    var avatarLink: String = "",
    var lastVisit: String = "",
    var lastAppointment: Appointment? = null,
    var nextAppointment: Appointment? = null
)