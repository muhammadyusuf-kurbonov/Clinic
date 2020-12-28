package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

data class Data(
    val __v: Int,
    val _id: String,
    val allow_general_sms: Boolean,
    val allow_promo_sms: Boolean,
    val avatar: CustomerAvatar,
    val avatar_id: String,
    val balance: Long,
    val birth_date: String,
    val blocked: Boolean,
    val can_delete: Boolean,
    val can_update: Boolean,
    val companyId: String,
    val createdAt: String,
    val customerIndex: String,
    val debtReturn: DebtReturn,
    val deleted: Boolean,
    val discountPercent: Int,
    val elementaryExam: ElementaryExam,
    val first_name: String,
    val gender: String,
    val lastVisit: String,
    val last_name: String,
    val phone: String,
    val schedule: List<Any>,
    val source: SourceX,
    val stats: Stats,
    val tags: List<String>,
    val updatedAt: String,
    val userId: List<String>
)