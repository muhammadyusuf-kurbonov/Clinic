package uz.muhammadyusuf.kurbonov.myclinic.core.states

sealed class ReportState {
    object Sending : ReportState()
    object Submitted : ReportState()
    object AskToAddNewCustomer : ReportState()
    class PurposeRequested(val communicationId: String) : ReportState()
    object ConnectionFailed : ReportState()
    object Default : ReportState()
}