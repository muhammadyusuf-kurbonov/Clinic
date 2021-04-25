package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class Role(
    val __v: Int, // 0
    val _id: String, // 5dcc15825faf3ce389b24ab7
    val companyId: Any, // null
    val createdAt: String, // 2019-11-13T14:38:58.154Z
    val description: String, // Директор
    val permissions: List<Permission>,
    val role: String, // SuperAdmin
    val updatedAt: String // 2019-11-13T14:38:58.154Z
)