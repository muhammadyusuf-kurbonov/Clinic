package uz.muhammadyusuf.kurbonov.myclinic.network.communications.response


import androidx.annotation.Keep

@Keep
data class Settings(
    val CalendarCurrent: CalendarCurrent,
    val HideCalendarPrevious: Boolean, // false
    val calendarStep: Int, // 30
    val staffQuery: List<Any>,
    val workPlaceQuery: List<Any>
)