package uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels

sealed class AuthResult {
    class Success(val token: String) : AuthResult()
    class ConnectionFailed(val exception: Exception) : AuthResult()
    object InvalidCredentials : AuthResult()
}
