package uz.muhammadyusuf.kurbonov.myclinic

import kotlinx.coroutines.flow.MutableStateFlow

object EventBus {
    val event = MutableStateFlow(0)
}