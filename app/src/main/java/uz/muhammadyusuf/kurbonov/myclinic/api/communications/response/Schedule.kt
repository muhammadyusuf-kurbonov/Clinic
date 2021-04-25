package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class Schedule(
    val _id: String, // 5e0609d632e289a267f6bdb1
    val dayOfWeek: String, // monday
    val endHour: Int, // 20
    val endMinute: String, // 00
    val startHour: Int, // 9
    val startMinute: String // 00
)