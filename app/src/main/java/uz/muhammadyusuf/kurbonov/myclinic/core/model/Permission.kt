package uz.muhammadyusuf.kurbonov.myclinic.core.model

data class Permission(
    val permissionId: String,
    val permissionTitle: String,
    val permissionSummary: String,
    val granted: Boolean
)
