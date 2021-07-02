package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search

data class ServiceXX(
    val __v: Int,
    val _id: String,
    val blocked: Boolean,
    val can_delete: Boolean,
    val can_update: Boolean,
    val companyId: String,
    val createdAt: String,
    val label: String,
    val priority: Int,
    val `public`: Boolean,
    val totalTreatments: Int,
    val updatedAt: String
)