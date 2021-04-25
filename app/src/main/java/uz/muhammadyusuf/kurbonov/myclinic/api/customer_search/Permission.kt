package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class Permission(
    val __v: Int,
    val _id: String,
    val companyId: Any,
    val create: Boolean,
    val createdAt: String,
    val delete: Boolean,
    val edit: Boolean,
    val resource: String,
    val roleId: String,
    val seeAll: Boolean,
    val updatedAt: String,
    val view: Boolean
)