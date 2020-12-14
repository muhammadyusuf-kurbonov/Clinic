package uz.muhammadyusuf.kurbonov.myclinic

import kotlinx.coroutines.flow.MutableStateFlow

object Bus {
    val state = MutableStateFlow("")
}