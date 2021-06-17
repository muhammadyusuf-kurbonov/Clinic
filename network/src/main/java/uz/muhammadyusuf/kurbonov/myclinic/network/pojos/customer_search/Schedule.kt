package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search

data class Schedule(
    val _id: String,
    val dayOfWeek: String,
    val endHour: Int,
    val endMinute: String,
    val startHour: Int,
    val startMinute: String
)