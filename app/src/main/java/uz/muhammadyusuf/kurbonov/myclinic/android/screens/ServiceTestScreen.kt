package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import uz.muhammadyusuf.kurbonov.myclinic.android.workers.SearchWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController
import uz.muhammadyusuf.kurbonov.myclinic.core.CallDirection

@Composable
fun ServiceTestScreen() {
    val context = LocalContext.current
    Box {
        Column {
            Column {
                ServiceControlButton(onClick = {
                    WorkManager.getInstance(context)
                        .enqueueUniqueWork(
                            "main",
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<SearchWorker>()
                                .setInputData(
                                    workDataOf(
                                        "phone" to "+998913975538"
                                    )
                                )
                                .build()
                        )
                }, text = "Start service")

                ServiceControlButton(onClick = {
                    AppStatesController.pushAction(Action.Search("+998913975539"))
                }, text = "Search - not found")

                ServiceControlButton(onClick = {
                    AppStatesController.pushAction(
                        Action.Report(5, CallDirection.INCOMING, false)
                    )
                }, text = "Report")

                ServiceControlButton(onClick = {
                    AppStatesController.pushAction(
                        Action.Report(0, CallDirection.INCOMING, true)
                    )
                }, text = "Report missed")

                ServiceControlButton(onClick = {
                    AppStatesController.pushAction(Action.Search("+998903500490"))
                }, text = "Search - found")

                ServiceControlButton(onClick = {
                    WorkManager.getInstance(context)
                        .cancelUniqueWork(
                            "main",
                        )
                }, text = "Stop service")
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