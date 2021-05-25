package uz.muhammadyusuf.kurbonov.myclinic

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.view.OverlayCompose


@Preview
@Composable
fun PreviewSearch() {
    OverlayCompose(State.Searching)
}

@Preview
@Composable
fun PreviewNotFound() {
    OverlayCompose(State.NotFound)
}

@Preview
@Composable
fun PreviewAuthRequest() {
    OverlayCompose(State.AuthRequest("+998913975538"))
}

@Preview
@Composable
fun PreviewNone() {
    OverlayCompose(State.None)
}

@Preview
@Composable
fun PreviewAddNewCustomer() {
    OverlayCompose(State.AddNewCustomerRequest("+998913975538"))
}