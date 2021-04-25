package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class Service(
    val duration: Int,
    val quantity: Int,
    val startAt: String,
    val treatment: Treatment?,
    val treatmentId: String,
    val user: User?,
    val userId: String
)