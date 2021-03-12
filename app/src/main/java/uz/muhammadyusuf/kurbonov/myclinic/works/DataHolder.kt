package uz.muhammadyusuf.kurbonov.myclinic.works

import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

object DataHolder {
    var phoneNumber: String = ""
        set(value) {
            if (value.isEmpty() || value.isBlank()) return
            if (field != value) {
                searchState = SearchStates.Loading
            }
            field = value
        }

    var communicationId: String? = null


    var type: CallTypes? = null

    var searchState: SearchStates = SearchStates.Loading
}

enum class CallTypes {
    INCOME, OUTCOME;

    fun getAsString(): String {
        return when (this) {
            INCOME -> "incoming"
            OUTCOME -> "outgoing"
        }
    }
}