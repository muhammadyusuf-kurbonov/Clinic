package uz.muhammadyusuf.kurbonov.myclinic.works

import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

object DataHolder {
    var phoneNumber: String = ""
        set(value) {
            if (value.isEmpty() || value.isBlank()) return
            if (field != value) {
                searchState = State.Loading
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

    var type: CallDirection? = null

    var searchState: State = State.Loading
        set(value) {
            communicationId = if (value !is State.Found)
                null
            else
                value.customer.id
            field = value
        }

    override fun toString(): String {
        return "phone number: $phoneNumber," +
                "communicationId: $communicationId," +
                "type: $type," +
                "searchState: $searchState"
    }
}

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