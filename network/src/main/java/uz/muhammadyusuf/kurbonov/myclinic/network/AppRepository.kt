package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationId
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO

interface AppRepository {
    suspend fun search(phone: String): CustomerDTO

    suspend fun sendCommunicationInfo(
        customerId: String,
        status: CommunicationStatus,
        duration: Long,
        type: CommunicationType // call direction
    ): CommunicationId

    suspend fun updateCommunicationNote(
        communicationId: String,
        note: String
    )

    suspend fun authenticate(username: String, password: String): AuthToken

    suspend fun addNewCustomer(
        firstName: String,
        lastName: String,
        phone: String
    )

    companion object {
        operator fun invoke(
            token: String,
            baseUrl: String = "https://app.32desk.com:3030/"
        ): AppRepository =
            AppRepositoryImpl(token, baseUrl)
    }
}