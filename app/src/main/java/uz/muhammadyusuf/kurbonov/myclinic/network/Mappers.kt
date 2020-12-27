package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.model.Contact
import uz.muhammadyusuf.kurbonov.myclinic.network.customer_search.CustomerDTO
import uz.muhammadyusuf.kurbonov.myclinic.utils.reformatDate

fun CustomerDTO.toContact(): Contact {
    val data = this.data[0]
    return Contact(
        id = data._id,
        name = "${data.last_name} ${data.first_name}",
        phoneNumber = data.phone,
        balance = data.balance,
        avatarLink = data.avatar.url,
        lastVisit = data.lastVisit.reformatDate(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "dd MMM yyyy"
        )
    )
}