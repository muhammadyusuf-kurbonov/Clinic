package uz.muhammadyusuf.kurbonov.myclinic.core

sealed class Action {
    class Login(val username: String, val password: String) : Action()
    class Search(val phone: String) : Action()
    class Report(
        val duration: Long,
        val callDirection: CallDirection,
        val isMissed: Boolean
    ) : Action()

    class SetPurpose(val purpose: String) : Action()
}