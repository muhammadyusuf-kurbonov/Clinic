package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

data class CustomerDTO(
    val appointments: List<Appointment>,
    val `data`: List<Data>,
    val elementaryExam: ElementaryExamX,
    val limit: Int,
    val skip: Int,
    val total: Int
)