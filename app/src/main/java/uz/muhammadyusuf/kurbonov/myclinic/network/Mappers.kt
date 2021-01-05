package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.model.Appointment
import uz.muhammadyusuf.kurbonov.myclinic.model.Contact
import uz.muhammadyusuf.kurbonov.myclinic.model.Doctor
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.utils.reformatDate
import java.util.*

fun CustomerDTO.toContact(): Contact {
    val data = this.data[0]
    val last = this.appointments[0].prev
    val lastAppointment = if (last != null) {
        val user = last.services[0].user
        Appointment(
            last.startAt.reformatDate(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "dd MMM yyyy HH:mm",
                oldTimeZone = TimeZone.getTimeZone("UTC")
            ),
            if (user != null)
                Doctor(
                    user.firstName + " " + user.lastName,
                    user.avatar.url
                ) else null,
            last.services[0].treatment?.service?.label ?: "N/A"
        )
    } else null
    val next = this.appointments[0].next
    val nextAppointment = if (next != null) {
        val user = next.services[0].user
        Appointment(
            next.startAt.reformatDate(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "dd MMM yyyy HH:mm",
                oldTimeZone = TimeZone.getTimeZone("UTC")
            ),
            if (user != null)
                Doctor(
                    user.firstName + " " + user.lastName,
                    user.avatar.url
                ) else null,
            next.services[0].treatment?.service?.label ?: "N/A"
        )
    } else null
    return Contact(
        id = data._id,
        name = "${data.last_name} ${data.first_name}",
        phoneNumber = data.phone,
        balance = data.balance,
        avatarLink = data.avatar.url,
        lastAppointment = lastAppointment,
        nextAppointment = nextAppointment
    )
}