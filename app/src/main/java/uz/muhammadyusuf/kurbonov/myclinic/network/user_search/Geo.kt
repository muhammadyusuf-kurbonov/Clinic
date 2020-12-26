package uz.muhammadyusuf.kurbonov.myclinic.network.user_search

import androidx.annotation.Keep

@Keep
data class Geo(
    val lat: String,
    val lng: String
)