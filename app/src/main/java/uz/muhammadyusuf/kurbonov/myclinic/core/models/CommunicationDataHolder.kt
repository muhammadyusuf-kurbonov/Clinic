package uz.muhammadyusuf.kurbonov.myclinic.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunicationDataHolder(
    val phone: String,
    val status: String,
    val type: String,
    val duration: Long
) : Parcelable