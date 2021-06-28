package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import uz.muhammadyusuf.kurbonov.myclinic.android.workers.SearchWorker

@Composable
fun ServiceTestScreen() {
    val context = LocalContext.current
    Box {
        Column {
            Row {
                Button(onClick = {
                    WorkManager.getInstance(context)
                        .enqueueUniqueWork(
                            "main",
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<SearchWorker>().build()
                        )
                }) {
                    Text(text = "Start service", style = MaterialTheme.typography.button)
                }
                Button(onClick = {
                    WorkManager.getInstance(context)
                        .cancelUniqueWork(
                            "main",
                        )
                }) {
                    Text(text = "Stop service", style = MaterialTheme.typography.button)
                }
            }
        }
    }
}