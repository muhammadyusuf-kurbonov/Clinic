package uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels

sealed class SendConnectionResult {
    data class PurposeRequest(val communicationId: String) : SendConnectionResult()
    object Success : SendConnectionResult()
    object NotConnected : SendConnectionResult()
    data class Failed(val exception: Exception) : SendConnectionResult()
}
