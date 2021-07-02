package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState

@Composable
fun PurposeScreen() {
    val phone = LocalPhoneNumberProvider.current
    val controller = LocalAppControllerProvider.current

    var selectedPurpose by remember {
        mutableStateOf("default")
    }

    var otherPurpose by remember {
        mutableStateOf("")
    }
    val reportState by AppStateStore.reportState.collectAsState()
    if (reportState !is ReportState.PurposeRequested)
        throw IllegalStateException(
            "This screen is available only when " +
                    "reportState is Purpose requested. " +
                    "But it\'s $reportState"
        )

    val purposes = listOf(
        stringResource(id = R.string.make_appointment),
        stringResource(R.string.change_appointment),
        stringResource(id = R.string.ask_address),
        stringResource(id = R.string.additional_info),
    )


    Column {

        Text(
            text = stringResource(id = R.string.purpose_msg, phone),
            style = MaterialTheme.typography.subtitle1
        )

        for (purpose in purposes) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                RadioButton(selected = (selectedPurpose == purpose), onClick = {
                    selectedPurpose = purpose
                })
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = purpose)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            RadioButton(selected = (selectedPurpose == "other"), onClick = {
                selectedPurpose = "other"
            })
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = stringResource(id = R.string.other))
        }

        if (selectedPurpose == "other") {
            OutlinedTextField(
                value = otherPurpose, onValueChange = {
                    otherPurpose = it
                },
                modifier = Modifier
                    .padding(4.dp)
                    .offset(8.dp)
            )
        }


        Button(onClick = {
            controller.handle(
                Action.SetPurpose(
                    if (selectedPurpose != "other") selectedPurpose
                    else otherPurpose
                )
            )
        }) {
            Text(text = stringResource(id = R.string.send), style = MaterialTheme.typography.button)
        }
    }
}