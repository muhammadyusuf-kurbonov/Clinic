package uz.muhammadyusuf.kurbonov.myclinic.core.states

sealed class AuthState {
    object Default : AuthState()
    object Authenticating : AuthState()
    object AuthSuccess : AuthState()
    object AuthRequired : AuthState()
    object AuthFailed : AuthState()
    object ConnectionFailed : AuthState()
    object ValidationFailed : AuthState()
}