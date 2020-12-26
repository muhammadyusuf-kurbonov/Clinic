package uz.muhammadyusuf.kurbonov.myclinic.network.user_search

import androidx.annotation.Keep

@Keep
data class Company(
    val bs: String,
    val catchPhrase: String,
    val name: String
)