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
import uz.muhammadyusuf.kurbonov.myclinic.android.workers.SearchWorker
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel

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
                            OneTimeWorkRequestBuilder<SearchWorker>().build()
                        )
                }, text = "Start service")

                ServiceControlButton(onClick = {
                    AppViewModel.pushAction(Action.Search("+998913975539"))
                }, text = "Search - not found")

                ServiceControlButton(onClick = {
                    AppViewModel.pushAction(Action.Search("+998903500490"))
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