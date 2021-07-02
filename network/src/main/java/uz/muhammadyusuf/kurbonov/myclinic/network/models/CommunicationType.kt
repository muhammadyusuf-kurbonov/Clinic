package uz.muhammadyusuf.kurbonov.myclinic.network.models

enum class CommunicationType {
    INCOMING, OUTGOING;

    override fun toString(): String {
        return when (this) {
            INCOMING -> "incoming"
            OUTGOING -> "outgoing"
        }
    }
}