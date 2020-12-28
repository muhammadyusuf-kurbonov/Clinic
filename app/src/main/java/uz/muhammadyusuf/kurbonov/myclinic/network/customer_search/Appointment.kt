package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

data class Appointment(
    val prev: AppointmentItem?,
    val next: AppointmentItem?
)