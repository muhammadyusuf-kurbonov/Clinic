package uz.muhammadyusuf.kurbonov.myclinic.utils

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.collect
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.AppEvent
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.EventBus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 *
 *  Return true if auth is successful, else false
 *
 * */
suspend fun authenticate(context: Context) = suspendCoroutine<Boolean> { continuation ->
    suspend {
        EventBus.event.collect {
            if (it is AppEvent.AuthSucceedEvent) {
                continuation.resume(true)
                EventBus.event.value = AppEvent.NoEvent
            } else if (it is AppEvent.AuthFailedEvent) {
                continuation.resume(false)
                EventBus.event.value = AppEvent.NoEvent
            }
        }
    }

    context.startActivity(Intent(context, LoginActivity::class.java))
}