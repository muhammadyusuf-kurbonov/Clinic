package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.communications.response


import androidx.annotation.Keep

@Keep
data class Service(
    val __v: Int, // 0
    val _id: String, // 5dc3c8229a76124f537464fc
    val blocked: Boolean, // true
    val can_delete: Boolean, // false
    val can_update: Boolean, // false
    val companyId: String, // 5dc2bf249a76124f5374648b
    val createdAt: String, // 2019-11-07T07:30:42.859Z
    val label: String, // Диагностика
    val priority: Int, // 2
    val `public`: Boolean, // true
    val totalTreatments: Int, // 8
    val updatedAt: String // 2021-02-23T12:59:51.466Z
)