package uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels

sealed class NewCustomerRequestResult {
    object Success : NewCustomerRequestResult()
    class Failed(val exception: Exception) : NewCustomerRequestResult()
}
