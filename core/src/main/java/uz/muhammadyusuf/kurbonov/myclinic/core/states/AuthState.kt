package uz.muhammadyusuf.kurbonov.myclinic.core.states

sealed class AuthState {
    object Default : AuthState()
    object AuthSuccess : AuthState()
    object AuthRequired : AuthState()
    object ConnectionFailed : AuthState()
    class FieldRequired(val fieldName: String) : AuthState()
}