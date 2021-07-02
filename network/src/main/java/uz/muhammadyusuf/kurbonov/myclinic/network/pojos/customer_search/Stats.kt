package uz.muhammadyusuf.kurbonov.myclinic.network.pojos.customer_search

data class Stats(
    val arrived: Int,
    val cancelled: Int,
    val completed: Int,
    val confirmed: Int,
    val new: Int,
    val notConfirmed: Int,
    val started: Int
)