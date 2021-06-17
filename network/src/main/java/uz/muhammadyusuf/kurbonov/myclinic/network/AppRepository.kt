package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.network.resultmodels.*

interface AppRepository {
    suspend fun search(phone: String): SearchResult

    suspend fun sendCommunicationInfo(
        customerId: String,
        status: String,
        duration: Long,
        callDirection: String
    ): SendConnectionResult

    suspend fun sendCommunicationNote(
        communicationId: String,
        body: String
    ): PatchConnectionResult

    suspend fun authenticate(username: String, password: String): AuthResult

    suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        phone: String
    ): NewCustomerRequestResult

    companion object {
        operator fun invoke(
            token: String,
            baseUrl: String = "https://app.32desk.com:3030/"
        ): AppRepository =
            AppRepositoryImpl(token, baseUrl)
    }
}