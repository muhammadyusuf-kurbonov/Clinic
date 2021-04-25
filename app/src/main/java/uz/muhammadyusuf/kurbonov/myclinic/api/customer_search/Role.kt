package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class Role(
    val __v: Int,
    val _id: String,
    val companyId: Any,
    val createdAt: String,
    val description: String,
    val permissions: List<Permission>,
    val role: String,
    val updatedAt: String
)