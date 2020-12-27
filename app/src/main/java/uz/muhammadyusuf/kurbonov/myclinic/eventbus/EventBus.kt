package uz.muhammadyusuf.kurbonov.myclinic.eventbus

import kotlinx.coroutines.flow.MutableStateFlow

object EventBus {
    val event = MutableStateFlow<AppEvent>(AppEvent.NoEvent)
}