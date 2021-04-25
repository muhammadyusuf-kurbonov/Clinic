package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class CalendarCurrent(
    val label: String, // Week
    val option: Option,
    val value: String // week
)