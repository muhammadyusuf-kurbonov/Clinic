package uz.muhammadyusuf.kurbonov.myclinic.eventbus

sealed class AppEvent {
    object NoEvent : AppEvent()

    object StopServiceEvent : AppEvent()
    object RestoreServiceEvent : AppEvent()

    object AuthFailedEvent : AppEvent()
    object AuthSucceedEvent : AppEvent()
}