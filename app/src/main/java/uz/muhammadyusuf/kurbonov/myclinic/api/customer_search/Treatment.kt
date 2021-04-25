package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class Treatment(
    val __v: Int,
    val _id: String,
    val blocked: Boolean,
    val bundle: List<Any>,
    val can_delete: Boolean,
    val can_update: Boolean,
    val code: String,
    val companyId: String,
    val createdAt: String,
    val deleted: Boolean,
    val duration: Int,
    val icd: List<Any>,
    val id: String,
    val label: String,
    val manipulations: List<Any>,
    val price: Int,
    val `public`: Boolean,
    val reminder: Reminder,
    val service: ServiceX,
    val serviceId: String,
    val updatedAt: String
)