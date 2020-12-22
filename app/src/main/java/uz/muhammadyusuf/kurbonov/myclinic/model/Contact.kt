package uz.muhammadyusuf.kurbonov.myclinic.model

import androidx.annotation.Keep
import uz.muhammadyusuf.kurbonov.myclinic.network.Address
import uz.muhammadyusuf.kurbonov.myclinic.network.Company

@Keep
data class Contact(
    var id: Int = 0,
    var name: String = "",
    var phoneNumber: String = "",
    var address: Address? = null,
    var company: Company? = null,
    var card_id: String = ""
)