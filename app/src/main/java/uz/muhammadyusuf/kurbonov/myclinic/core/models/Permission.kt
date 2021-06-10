package uz.muhammadyusuf.kurbonov.myclinic.core.models

data class Permission(
    val permissionId: String,
    val permissionTitle: String,
    val permissionSummary: String,
    val granted: Boolean
)
