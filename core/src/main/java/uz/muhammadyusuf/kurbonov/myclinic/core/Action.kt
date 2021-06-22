package uz.muhammadyusuf.kurbonov.myclinic.core

sealed class Action {
    class Login(val username: String, val password: String) : Action()
    data class Search(val phone: String) : Action()
}