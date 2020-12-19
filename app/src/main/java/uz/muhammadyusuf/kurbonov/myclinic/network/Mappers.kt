package uz.muhammadyusuf.kurbonov.myclinic.network

import uz.muhammadyusuf.kurbonov.myclinic.model.Contact

fun UserDTO.toContact(): Contact? {
    if (this.item == null)
        return null
    return Contact(
        id = item.id,
        name = item.name,
        phoneNumber = item.phone,
        address = item.address,
        company = item.company
    )
}