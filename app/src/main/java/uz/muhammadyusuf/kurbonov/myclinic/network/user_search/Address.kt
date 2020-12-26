package uz.muhammadyusuf.kurbonov.myclinic.network.user_search

import androidx.annotation.Keep

@Keep
data class Address(
    val city: String,
    val geo: Geo,
    val street: String,
    val suite: String,
    val zipcode: String
) {
    override fun toString(): String {
        return "$zipcode $city, $street $suite"
    }
}