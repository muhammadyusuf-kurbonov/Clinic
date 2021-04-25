package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class Appointment(
    val prev: AppointmentItem?,
    val next: AppointmentItem?
)