package uz.muhammadyusuf.kurbonov.myclinic.network

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