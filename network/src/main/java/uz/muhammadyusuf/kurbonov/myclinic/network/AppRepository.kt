package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.network.models.AuthToken
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationId
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationStatus
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.treatment.TreatmentDTO
import uz.muhammadyusuf.kurbonov.myclinic.network.pojos.users.UserDTO

interface AppRepository {
    var token: String
    /**
     * This method is for searching customer by phone-number
     *
     * @throws CustomerNotFoundException if nobody found !must be catch!
     * @throws AuthRequestException if token is expired
     * @throws NotConnectedException if no internet connection
     * @throws APIException if error with API occurred
     *
     * @return found customer
     */
    suspend fun search(phone: String): CustomerDTO

    suspend fun getUser(id: String): UserDTO.Data

    suspend fun getTreatment(id: String): TreatmentDTO.Data

    suspend fun sendCommunicationInfo(
        customerId: String,
        status: CommunicationStatus,
        duration: Long,
        type: CommunicationType // call direction
    ): CommunicationId

    /**
     *  This method updates (patch) communications entity on backend
     *  @throws AuthRequestException - when username or password are incorrect
     *  @throws NotConnectedException - if there were problems with network
     *  @throws APIException - if username or password are empty
     */
    suspend fun updateCommunicationNote(
        communicationId: String,
        note: String
    )

    /**
     *  This method is for authentication
     *
     *  @param username must not be empty
     *  @param password must not be empty
     *
     *  @return token if call was successful
     *  @throws AuthRequestException - when username or password are incorrect
     *  @throws NotConnectedException - if there were problems with network
     *  @throws APIException - if username or password are empty
     */
    suspend fun authenticate(username: String, password: String): AuthToken

    /**
     * This method register new customer in system
     *
     * @param firstName must not be empty
     * @param phone must not be empty
     *
     *  @throws AuthRequestException - when auth required (token expired)
     *  @throws NotConnectedException - if there were problems with network
     *  @throws APIException - if first_name or phone are empty
     */
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