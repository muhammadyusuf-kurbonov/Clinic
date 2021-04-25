package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class User(
    val __v: Int, // 0
    val _id: String, // 5dc2beab0af9c9e30a0ea0f5
    val allowLogin: Boolean, // true
    val avatar: AvatarX,
    val avatar_id: String, // 5dcc321c8b6ab5f01d2f4efd
    val balance: Double, // 29194701.935951814
    val birthDate: String, // 2019-11-06T00:00:00.000Z
    val blocked: Boolean, // true
    val can_delete: Boolean, // false
    val can_update: Boolean, // false
    val color: String, // #b6e6bd
    val companyId: String, // 5dc2bf249a76124f5374648b
    val createdAt: String, // 2019-11-06T12:38:03.788Z
    val deleted: Boolean, // false
    val email: String, // demo@32desk.com
    val firstName: String, // Anor
    val gender: String, // female
    val isVerified: Boolean, // true
    val lastName: String, // Rozax
    val password: String, // $2a$14$lbMt6d22AOiXeraEql0dReiOYTDYOMvgaSTFLs.gt7QOl2LgYSqVm
    val primaryPhone: String, // +469(99) 999-99-96
    val `public`: Boolean, // true
    val recentPresets: List<String>,
    val recentTreatments: List<String>,
    val revenuePercent: RevenuePercent,
    val role: Role,
    val roleId: String, // 5dcc15825faf3ce389b24ab7
    val schedule: List<Schedule>,
    val settings: Settings,
    val source: SourceX,
    val specialtyId: String, // 5d75ee37ef2d1b282cef3915
    val treatmentNames: List<TreatmentName>,
    val treatments: List<String>,
    val treatmentsPercent: List<TreatmentsPercent>,
    val updatedAt: String, // 2021-03-09T18:06:58.347Z
    val verifyExpires: String, // 2019-11-11T12:38:03.777Z
    val verifyToken: String // 53d1f94f261e4625e811f1244ec1cd
)