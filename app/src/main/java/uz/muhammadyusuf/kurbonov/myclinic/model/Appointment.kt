package uz.muhammadyusuf.kurbonov.myclinic.model

data class Appointment(
    var date: String,
    var doctor: Doctor,
    var diagnosys: String
)