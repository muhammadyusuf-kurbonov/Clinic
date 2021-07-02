package uz.muhammadyusuf.kurbonov.myclinic.core.states

sealed class RegisterState {
    object Registering : RegisterState()
    object RegisterSuccess : RegisterState()
    object VerificationFailed : RegisterState()
    object Default : RegisterState()
    object ConnectionFailed : RegisterState()
}