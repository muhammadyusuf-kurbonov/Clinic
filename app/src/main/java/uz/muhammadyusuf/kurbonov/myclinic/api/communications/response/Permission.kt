package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class Permission(
    val __v: Int, // 0
    val _id: String, // 5dcc15825faf3ce389b24ab8
    val companyId: Any, // null
    val create: Boolean, // true
    val createdAt: String, // 2019-11-13T14:38:58.231Z
    val delete: Boolean, // true
    val edit: Boolean, // true
    val resource: String, // users
    val roleId: String, // 5dcc15825faf3ce389b24ab7
    val seeAll: Boolean, // true
    val updatedAt: String, // 2020-06-26T12:33:59.781Z
    val view: Boolean // true
)