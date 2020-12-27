package uz.muhammadyusuf.kurbonov.myclinic.network.customer_search

data class CustomerDTO(
    val archived: Int,
    val `data`: List<Data>,
    val debtors: Int,
    val elementaryExam: ElementaryExamX,
    val limit: Int,
    val meta: List<Meta>,
    val prepaid: Int,
    val skip: Int,
    val total: Int,
    val totalCustomers: Int
)