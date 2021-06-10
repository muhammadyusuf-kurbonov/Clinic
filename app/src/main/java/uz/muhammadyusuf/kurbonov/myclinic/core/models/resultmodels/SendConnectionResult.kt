package uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels

sealed class SendConnectionResult {
    data class PurposeRequest(val communicationId: String) : SendConnectionResult()
    object Success : SendConnectionResult()
    object NotConnected : SendConnectionResult()
    data class Failed(val exception: Exception) : SendConnectionResult()
}
