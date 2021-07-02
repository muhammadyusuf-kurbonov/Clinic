package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.treatment


import androidx.annotation.Keep

@Keep
data class TreatmentDTO(
    val `data`: List<Data>,
    val limit: Int, // 50
    val skip: Int, // 0
    val total: Int // 1
) {
    @Keep
    data class Data(
        val __v: Int, // 0
        val _id: String, // 5dc3c84d9a76124f537464fe
        val blocked: Boolean, // false
        val bufferDuration: Any, // null
        val bundle: List<Any>,
        val can_delete: Boolean, // true
        val can_update: Boolean, // true
        val code: String, // Д003
        val companyId: String, // 5dc2bf249a76124f5374648b
        val createdAt: String, // 2019-11-07T07:31:25.398Z
        val deleted: Boolean, // false
        val duration: Int, // 60
        val icd: List<Any>,
        val label: String, // Первичная консультация специалиста с КТ и составлением плана лечения
        val manipulations: List<Any>,
        val price: Int, // 60000
        val `public`: Boolean, // true
        val reminder: Reminder,
        val service: Service,
        val serviceId: String, // 5dc3c8229a76124f537464fc
        val shortcode: String, // 3
        val updatedAt: String // 2020-02-20T22:03:20.041Z
    ) {
        @Keep
        class Reminder

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
            val priority: Int, // 3
            val `public`: Boolean, // true
            val totalTreatments: Int, // 7
            val updatedAt: String // 2021-06-16T12:04:27.914Z
        )
    }
}