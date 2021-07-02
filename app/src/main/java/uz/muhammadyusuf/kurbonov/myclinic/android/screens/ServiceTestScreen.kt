package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.workers.SearchWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.CallDirection

@Composable
fun ServiceTestScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.padding(8.dp)) {
        var phone by remember {
            mutableStateOf("")
        }
        Column {
            val appStatesController = LocalAppControllerProvider.current
            var startTime = remember {
                System.currentTimeMillis()
            }
            OutlinedTextField(value = phone, onValueChange = { phone = it })
            Row {
                ServiceControlButton(
                    onClick = {
                        WorkManager.getInstance(context).enqueueUniqueWork(
                            "main",
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<SearchWorker>()
                                .setInputData(
                                    workDataOf(
                                        "phone" to phone
                                    )
                                ).build()
                        )
                        appStatesController.handle(Action.Search(phone))
                        startTime = System.currentTimeMillis()
                    },
                    text = "Start call"
                )
                ServiceControlButton(
                    onClick = {
                        val duration = System.currentTimeMillis() - startTime
                        appStatesController.handle(
                            Action.Report(
                                duration / 1000L, CallDirection.INCOMING, false
                            )
                        )
                    },
                    text = "End call"
                )
            }
        }
    }
}

@Composable
internal fun ServiceControlButton(
    onClick: () -> Unit,
    text: String
) {
    Button(onClick = {
        onClick()
    }, modifier = Modifier.padding(4.dp)) {
        Text(text = text, style = MaterialTheme.typography.button)
    }
}