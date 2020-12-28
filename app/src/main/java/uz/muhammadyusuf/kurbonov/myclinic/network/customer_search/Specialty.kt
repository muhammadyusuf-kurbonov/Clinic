package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

data class Specialty(
    val __v: Int,
    val _id: String,
    val blocked: Boolean,
    val can_delete: Boolean,
    val can_update: Boolean,
    val codename: String,
    val createdAt: String,
    val icd: List<Any>,
    val label: String,
    val language: String,
    val updatedAt: String
)