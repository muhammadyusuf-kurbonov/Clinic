package uz.muhammadyusuf.kurbonov.myclinic.core.login

sealed class LoginActions {
    class Login(val username: String, val password: String) : LoginActions()
}