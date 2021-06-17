package uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels

sealed class PatchConnectionResult {
    object Success : PatchConnectionResult()
    class Failed(val exception: Exception) : PatchConnectionResult()
}
