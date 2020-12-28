package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.model.Appointment
import uz.muhammadyusuf.kurbonov.myclinic.model.Contact
import uz.muhammadyusuf.kurbonov.myclinic.model.Doctor
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.utils.reformatDate

fun CustomerDTO.toContact(): Contact {
    val data = this.data[0]
    val last = this.appointments[0].prev
    val lastAppointment = if (last != null)
        Appointment(
            last.startAt.reformatDate(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "dd MMM yyyy"
            ),
            Doctor(
                last.services[0].user.firstName + " " + last.services[0].user.lastName,
                last.services[0].user.avatar.url
            ),
            last.services[0].treatment.service.label
        ) else null
    val next = this.appointments[0].next
    val nextAppointment = if (next != null)
        Appointment(
            next.startAt.reformatDate(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "dd MMM yyyy"
            ),
            Doctor(
                next.services[0].user.firstName + " " + next.services[0].user.lastName,
                next.services[0].user.avatar.url
            ),
            next.services[0].treatment.service.label
        ) else null
    return Contact(
        id = data._id,
        name = "${data.last_name} ${data.first_name}",
        phoneNumber = data.phone,
        balance = data.balance,
        avatarLink = data.avatar.url,
        lastVisit = data.lastVisit.reformatDate(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "dd MMM yyyy"
        ),
        lastAppointment = lastAppointment,
        nextAppointment = nextAppointment
    )
}