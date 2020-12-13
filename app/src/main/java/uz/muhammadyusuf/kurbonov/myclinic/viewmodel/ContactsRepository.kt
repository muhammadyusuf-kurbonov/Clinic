package uz.muhammadyusuf.kurbonov.myclinic.viewmodel

import uz.muhammadyusuf.kurbonov.myclinic.model.Contact

interface ContactsRepository {
    suspend fun getContact(phone: String): Contact?
}