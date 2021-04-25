package uz.muhammadyusuf.kurbonov.myclinic.api.communications.response


import androidx.annotation.Keep

@Keep
data class CommunicationResponse(
    val __v: Int, // 0
    val _id: String, // 6047c8c723debe0785b55b38
    val body: String,
    val cancelled: Boolean, // false
    val companyId: String, // 5dc2bf249a76124f5374648b
    val createdAt: String, // 2021-03-09T19:13:11.352Z
    val customer: Customer,
    val customerId: String, // 602671e9cdca6a894deab66f
    val duration: Int, // 7
    val status: String, // accepted
    val transport: String, // phone
    val type: String, // incoming
    val updatedAt: String, // 2021-03-09T19:13:11.352Z
    val user: User,
    val userId: String // 5dc2beab0af9c9e30a0ea0f5
)