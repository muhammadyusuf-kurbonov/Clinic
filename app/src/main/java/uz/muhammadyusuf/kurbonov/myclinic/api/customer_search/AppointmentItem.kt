package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class AppointmentItem(
    val __v: Int,
    val _id: String,
    val companyId: String,
    val createdAt: String,
    val customerId: String,
    val endAt: String,
    val notify: Boolean,
    val services: List<Service>,
    val startAt: String,
    val status: String,
    val totalPrice: Int,
    val updatedAt: String,
    val userId: String
)