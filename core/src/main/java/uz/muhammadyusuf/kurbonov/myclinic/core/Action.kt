package uz.muhammadyusuf.kurbonov.myclinic.core

import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection

sealed class Action {
    object Start : Action()
    object SetNoConnectionState : Action()
    class Search(val phoneNumber: String, val direction: CallDirection) : Action()
    class SendCommunicationNote(val communicationId: String, val body: String) : Action()
    class EndCall(val phone: String) : Action()
    object Finish : Action()
    object Restart : Action()
    object None : Action()

    enum class ViewType {
        NOTIFICATION, OVERLAY
    }
}
