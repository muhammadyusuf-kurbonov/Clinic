package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.authentification

data class AuthRequest(
    val email: String,
    val password: String,
    val strategy: String = "local"
)