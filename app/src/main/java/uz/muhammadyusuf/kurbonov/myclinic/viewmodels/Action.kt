package uz.muhammadyusuf.kurbonov.myclinic.viewmodels

import android.content.Context
import uz.muhammadyusuf.kurbonov.myclinic.works.CallDirection

sealed class Action {
    class Start(val context: Context) : Action()
    class SetCallDirection(val callDirection: CallDirection) : Action()
    object SetNoConnectionState : Action()
    class Search(val phoneNumber: String) : Action()
    object Finish : Action()
}
