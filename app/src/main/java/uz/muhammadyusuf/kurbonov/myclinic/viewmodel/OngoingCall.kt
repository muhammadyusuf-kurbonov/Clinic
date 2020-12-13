package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import android.telecom.Call
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

object OngoingCall {
    val state: MutableStateFlow<Int> = MutableStateFlow(0)

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            Timber.d(call.toString())
            state.value = newState
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                state.value = it.state
            }
            field = value
        }

    fun answer() {
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call!!.disconnect()
    }
}