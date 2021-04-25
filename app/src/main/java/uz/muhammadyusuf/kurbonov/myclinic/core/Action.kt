package uz.muhammadyusuf.kurbonov.myclinic.core

import android.content.Context
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection

sealed class Action {
    class Start(val context: Context) : Action()
    object SetNoConnectionState : Action()
    class Search(val phoneNumber: String, val direction: CallDirection) : Action()
    class EndCall(val context: Context, val phone: String) : Action()
    object Finish : Action()
    object Restart : Action()
}
