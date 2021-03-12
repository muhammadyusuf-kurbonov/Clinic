package uz.muhammadyusuf.kurbonov.myclinic.network.communications.response


import androidx.annotation.Keep

@Keep
data class DebtReturn(
    val date: Any, // null
    val notify: Boolean, // false
    val text: Any, // null
    val timeTemplate: TimeTemplate
)