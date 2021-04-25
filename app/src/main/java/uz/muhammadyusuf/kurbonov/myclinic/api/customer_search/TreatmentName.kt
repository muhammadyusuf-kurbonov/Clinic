package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class TreatmentName(
    val _id: String,
    val blocked: Boolean,
    val can_delete: Boolean,
    val can_update: Boolean,
    val icd: List<Any>,
    val label: String,
    val service: ServiceXX,
    val serviceId: String
)