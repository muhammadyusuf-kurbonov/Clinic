package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.users

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserDTO(
    val `data`: List<Data>,
    val limit: Int, // 50
    val skip: Int, // 0
    val total: Int // 1
) {
    data class Data(
        val __v: Int, // 0
        val _id: String, // 5dc2beab0af9c9e30a0ea0f5
        val allowLogin: Boolean, // true
        val avatar: Avatar,
        val avatar_id: String, // 5dcc321c8b6ab5f01d2f4efd
        val balance: Double, // 57097242.98998302
        val birthDate: String, // 2019-11-06T00:00:00.000Z
        val blocked: Boolean, // false
        val can_delete: Boolean, // true
        val can_update: Boolean, // true
        val color: String, // #b6e6bd
        val companyId: String, // 5dc2bf249a76124f5374648b
        val createdAt: String, // 2019-11-06T12:38:03.788Z
        val deleted: Boolean, // false
        val email: String, // demo@32desk.com
        val firstName: String, // Anor
        val gender: String, // female
        val isVerified: Boolean, // true
        val lastName: String, // Rozax
        val mobileSettings: MobileSettings,
        val primaryPhone: String, // +469(99) 999-99-96
        val `public`: Boolean, // true
        val recentPresets: List<String>,
        val recentTreatments: List<String>,
        val revenuePercent: RevenuePercent,
        val role: Role,
        val roleId: String, // 5dcc15825faf3ce389b24ab7
        val schedule: List<Schedule>,
        val settings: Settings,
        val source: Source,
        val specialtyId: String, // 5d75ee37ef2d1b282cef3915
        val treatmentNames: List<TreatmentName>,
        val treatments: List<String>,
        val treatmentsPercent: List<TreatmentsPercent>,
        val updatedAt: String, // 2021-06-28T07:05:30.060Z
        val verifyExpires: String, // 2019-11-11T12:38:03.777Z
        val verifyToken: String // 53d1f94f261e4625e811f1244ec1cd
    ) {
        data class Avatar(
            val __v: Int, // 0
            val _id: String, // 5dcc321c8b6ab5f01d2f4efd
            val category: String, // profile
            val companyId: String, // 5dc2bf249a76124f5374648b
            val createdAt: String, // 2019-11-13T16:41:00.659Z
            val filename: String, // 5dc2bf249a76124f5374648b/520202260ad57592d20299b5cd6f9ac5f84d06de406f720e240a9fed4a99c02b.jpeg
            val label: String, // bigstock-Portrait-of-a-friendly-female-26984102-2-238x300.jpg
            val mimetype: String, // image/jpeg
            val preview: String, // http://app.32desk.com/uploads/5dc2bf249a76124f5374648b/thumb200-520202260ad57592d20299b5cd6f9ac5f84d06de406f720e240a9fed4a99c02b.jpeg
            val size: Int, // 11451
            val tags: List<String>,
            val tooth: Any, // null
            val updatedAt: String, // 2019-11-13T16:41:00.680Z
            val url: String, // http://app.32desk.com/uploads/5dc2bf249a76124f5374648b/520202260ad57592d20299b5cd6f9ac5f84d06de406f720e240a9fed4a99c02b.jpeg
            val user_id: String // 5dc2beab0af9c9e30a0ea0f5
        )

        data class MobileSettings(
            @SerializedName("CalendarCurrent")
            val calendarCurrent: CalendarCurrent,
            val HideCalendarPrevious: Boolean, // false
            val ShowCalendarCancelled: Boolean, // false
            val calendarStep: Int, // 60
            val staffQuery: List<Any>,
            val workPlaceQuery: List<Any>
        ) {
            data class CalendarCurrent(
                val label: String, // 3 Days
                val option: Option,
                val value: String // 3days
            ) {
                data class Option(
                    val type: String, // days
                    val value: Int // 3
                )
            }
        }

        data class RevenuePercent(
            val measure: String, // %
            val value: Int // 30
        )

        data class Role(
            val __v: Int, // 0
            val _id: String, // 5dcc15825faf3ce389b24ab7
            val companyId: Any, // null
            val createdAt: String, // 2019-11-13T14:38:58.154Z
            val description: String, // Директор
            val permissions: List<Permission>,
            val role: String, // SuperAdmin
            val updatedAt: String // 2019-11-13T14:38:58.154Z
        ) {
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
        }

        data class Schedule(
            val _id: String, // 5e0609d632e289a267f6bdb1
            val dayOfWeek: String, // monday
            val endHour: Int, // 20
            val endMinute: String, // 00
            val startHour: Int, // 9
            val startMinute: String // 00
        )

        data class Settings(
            @SerializedName("CalendarCurrent")
            val calendarCurrent: CalendarCurrent,
            val HideCalendarPrevious: Boolean, // false
            val ShowCalendarCancelled: Boolean, // false
            val calendarStep: Int, // 30
            val staffQuery: List<Any>,
            val workPlaceQuery: List<Any>
        ) {
            data class CalendarCurrent(
                val label: String, // Week
                val option: Option,
                val value: String // week
            ) {
                data class Option(
                    val type: String, // days
                    val value: Int // 7
                )
            }
        }

        class Source

        data class TreatmentName(
            val _id: String, // 5dc3c84d9a76124f537464fe
            val blocked: Boolean, // true
            val can_delete: Boolean, // false
            val can_update: Boolean, // false
            val icd: List<Any>,
            val label: String, // Первичная консультация специалиста с КТ и составлением плана лечения
            val service: Service,
            val serviceId: String // 5dc3c8229a76124f537464fc
        ) {
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

        data class TreatmentsPercent(
            val _id: String, // 5e294ada4d6426660f2567cc
            val measure: String, // %
            val treatmentId: String, // 5e13298c102502681992e30b
            val value: Int // 15
        )
    }
}