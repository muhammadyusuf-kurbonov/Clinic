package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.response


import androidx.annotation.Keep

@Keep
data class TreatmentName(
    val _id: String, // 5dc3c84d9a76124f537464fe
    val blocked: Boolean, // true
    val can_delete: Boolean, // false
    val can_update: Boolean, // false
    val icd: List<Any>,
    val label: String, // Первичная консультация специалиста с КТ и составлением плана лечения
    val service: Service,
    val serviceId: String // 5dc3c8229a76124f537464fc
)