package uz.muhammadyusuf.kurbonov.myclinic.api.customer_search

data class CustomerDTO(
    val appointments: List<Appointment>,
    val `data`: List<Data>,
    val elementaryExam: ElementaryExamX,
    val limit: Int,
    val skip: Int,
    val total: Int
)