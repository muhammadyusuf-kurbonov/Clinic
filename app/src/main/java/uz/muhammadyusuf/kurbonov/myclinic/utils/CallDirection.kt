package uz.muhammadyusuf.kurbonov.myclinic.utils

enum class CallDirection {
    INCOME, OUTGOING;

    fun getAsString(): String {
        return when (this) {
            INCOME -> "incoming"
            OUTGOING -> "outgoing"
        }
    }

    companion object {
        fun parseString(direction: String): CallDirection {
            return when (direction) {
                "outgoing" -> OUTGOING
                "incoming" -> INCOME
                else -> throw IllegalArgumentException("Invalid call direction: $direction")
            }
        }
    }
}