package uz.muhammadyusuf.kurbonov.myclinic.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow

object EventBus {
    val job = Job()
    val scope = CoroutineScope(job + Dispatchers.Main)
    val event = MutableStateFlow<AppEvent>(AppEvent.NoEvent)
}