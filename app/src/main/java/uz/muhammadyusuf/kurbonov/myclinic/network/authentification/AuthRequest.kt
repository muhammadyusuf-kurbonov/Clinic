package uz.muhammadyusuf.kurbonov.myclinic.network.authentification

data class AuthRequest(
    val email: String,
    val password: String,
    val strategy: String = "local"
)