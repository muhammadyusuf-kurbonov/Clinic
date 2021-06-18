package uz.muhammadyusuf.kurbonov.myclinic.core.utils

import uz.muhammadyusuf.kurbonov.myclinic.core.models.Appointment
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Doctor
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.AppointmentItem
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.shared.reformatDate
import java.util.*

fun CustomerDTO.toContact(): Customer {
    if (this.data.isEmpty())
        throw IllegalStateException("It should be not found")

    val data = this.data[0]

    val lastAppointment: Appointment? = this.appointments[0].prev.toAppointment()

    val nextAppointment: Appointment? = this.appointments[0].next.toAppointment()


    return Customer(
        id = data._id,
        name = "${data.last_name} ${data.first_name}",
        phoneNumber = data.phone,
        balance = data.balance,
        avatarLink = data.avatar.url,
        lastAppointment = lastAppointment,
        nextAppointment = nextAppointment
    )
}

private fun AppointmentItem?.toAppointment(): Appointment? {
    if (this != null && this.services.isNotEmpty()) {
        // user is doctor
        val user = this.services[0].user
        return Appointment(
            startAt.reformatDate(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "dd MMM yyyy HH:mm",
                oldTimeZone = TimeZone.getTimeZone("UTC")
            ),
            if (user != null)
                Doctor(
                    user.firstName + " " + user.lastName,
                    user.avatar.url
                ) else null,
            services[0].treatment?.service?.label ?: ""
        )
    } else {
        return null
    }


}