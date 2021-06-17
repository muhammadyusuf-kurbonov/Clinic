package uz.muhammadyusuf.kurbonov.myclinic.core

import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallInfo

interface SettingsProvider {
    fun getToken(): String
    fun getAutoCancelDelay(): Long
    fun getLastCallInfo(): CallInfo
    fun getViewType(): Action.ViewType
}