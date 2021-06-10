package uz.muhammadyusuf.kurbonov.myclinic.core

import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SearchResult
import uz.muhammadyusuf.kurbonov.myclinic.core.models.resultmodels.SendConnectionResult

interface AppRepository {
    suspend fun search(phone: String): SearchResult

    suspend fun sendCommunicationInfo(
        customerId: String,
        status: String,
        duration: Long,
        callDirection: String
    ): SendConnectionResult
}