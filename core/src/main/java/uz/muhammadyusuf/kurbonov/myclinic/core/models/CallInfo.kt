package uz.muhammadyusuf.kurbonov.myclinic.core.models

data class CallInfo(
    val phone: String,
    val status: String,
    val type: String,
    val duration: Long
)