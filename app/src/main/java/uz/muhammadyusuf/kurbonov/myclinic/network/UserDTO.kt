package uz.muhammadyusuf.kurbonov.myclinic.network

data class UserDTO(
    val code: String,
    val item: Item?,
    val status: String
)