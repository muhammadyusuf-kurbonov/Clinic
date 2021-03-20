package uz.muhammadyusuf.kurbonov.myclinic.works

import uz.muhammadyusuf.kurbonov.myclinic.App
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
        set(value) {
            App.pref.edit().putString("communicatedId", value).apply()
        }
        get() {
            return if (field == null)
                App.pref.getString("communicatedId", null)
            else
                field
        }

    var type: CallTypes? = null

    var searchState: SearchStates = SearchStates.Loading
        set(value) {
            communicationId = if (value !is SearchStates.Found)
                null
            else
                value.contact.id
            field = value
        }
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