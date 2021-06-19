package uz.muhammadyusuf.kurbonov.myclinic.network.models

enum class CommunicationStatus {
    ACCEPTED, MISSED;

    override fun toString(): String {
        return when (this) {
            ACCEPTED -> "accepted"
            MISSED -> "missed"
        }
    }
}