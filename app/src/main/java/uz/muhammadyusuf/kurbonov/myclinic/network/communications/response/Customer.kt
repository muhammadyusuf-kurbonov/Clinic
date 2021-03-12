package uz.muhammadyusuf.kurbonov.myclinic.network.communications.response


import androidx.annotation.Keep

@Keep
data class Customer(
    val __v: Int, // 0
    val _id: String, // 602671e9cdca6a894deab66f
    val allow_general_sms: Boolean, // true
    val allow_promo_sms: Boolean, // true
    val avatar: Avatar,
    val avatar_id: Any, // null
    val balance: Int, // 0
    val birth_date: String, // 1993-01-05T00:00:00.000Z
    val blocked: Boolean, // true
    val can_delete: Boolean, // false
    val can_update: Boolean, // false
    val companyId: String, // 5dc2bf249a76124f5374648b
    val createdAt: String, // 2021-02-12T12:17:45.211Z
    val customerIndex: String, // 1384
    val debtReturn: DebtReturn,
    val deleted: Boolean, // false
    val elementaryExam: ElementaryExam,
    val first_name: String, // Максимов
    val gender: String, // male
    val lastVisit: String, // 2021-02-22T10:00:00.000Z
    val last_name: String, // Михаил
    val middle_name: String, // Сергеевич
    val phone: String, // +998 99 480 14 16
    val schedule: List<Any>,
    val secondPhone: String, // +998 33 708 14 16
    val source: Source,
    val stats: Stats,
    val tags: List<Any>,
    val updatedAt: String, // 2021-02-26T17:38:22.689Z
    val userId: List<String>
)