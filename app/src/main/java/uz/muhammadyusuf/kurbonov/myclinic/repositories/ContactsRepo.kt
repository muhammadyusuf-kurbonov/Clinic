package uz.muhammadyusuf.kurbonov.myclinic.repositories

import uz.muhammadyusuf.kurbonov.myclinic.model.Contact
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.ContactsRepository

class ContactsRepo : ContactsRepository {

    private val contacts = listOf(
        Contact(name = "Muhammadyusuf", phoneNumber = "+998913975538"),
        Contact(name = "Mansur", phoneNumber = "+998337081416"),
        Contact(name = "Azizbek", phoneNumber = "+998941935100")
    )

    override suspend fun getContact(phone: String): Contact? {
        return contacts.firstOrNull {
            it.phoneNumber == phone
        }
    }

}