package uz.muhammadyusuf.kurbonov.myclinic.core.login

sealed class LoginStates {
    object AuthSuccess : LoginStates()
    object AuthFailed : LoginStates()
    object ConnectionFailed : LoginStates()
    class FieldRequired(val fieldName: String) : LoginStates()
}