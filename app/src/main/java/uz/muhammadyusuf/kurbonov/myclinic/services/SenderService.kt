package uz.muhammadyusuf.kurbonov.myclinic.services

import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.ExplainActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest

class SenderService : JobIntentService() {

    companion object {
        const val NOTIFICATION_ID = 425
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("start service of sengding $intent")
        if (intent != null) {
            val holder = intent.extras?.getParcelable<CommunicationDataHolder>("data")
                ?: throw IllegalStateException()

            Timber.d("$holder")

            val activityIntent = Intent(this@SenderService, ExplainActivity::class.java).apply {
                putExtra("data", holder)
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }

            val notification = NotificationCompat.Builder(this, "clinic_info")
                .apply {
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                    setOngoing(true)
                    setContentText("Please, specify purpose of call")
                    setContentIntent(
                        PendingIntent.getActivity(
                            this@SenderService,
                            0,
                            activityIntent,
                            0
                        )
                    )
                    setAutoCancel(true)
                }.build()
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, notification)
            startForeground(
                NOTIFICATION_ID, notification
            )

            val apiService = KoinJavaComponent.get(APIService::class.java)
            runBlocking {
                val searchCustomer =
                    apiService.searchCustomer(holder.phone, withAppointments = 0)

                if (searchCustomer.isSuccessful && searchCustomer.body()!!.data.isNotEmpty()) {

                    if (holder.status == "declined") {
                        val data = Data.Builder()
                        data.putString(SendStatusRequest.INPUT_PHONE, holder.phone)
                        data.putString(SendStatusRequest.INPUT_STATUS, holder.status)
                        data.putLong(
                            SendStatusRequest.INPUT_DURATION,
                            holder.duration
                        )
                        data.putString(SendStatusRequest.INPUT_NOTE, "")
                        data.putString(SendStatusRequest.INPUT_TYPE, holder.type)
                        val constraint = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                        val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
                            .setInputData(data.build())
                            .setConstraints(constraint)
                            .addTag("sender")
                            .build()
                        WorkManager.getInstance(this@SenderService)
                            .enqueueUniqueWork(
                                "sender",
                                ExistingWorkPolicy.REPLACE,
                                request
                            )

                        NotificationManagerCompat.from(this@SenderService)
                            .cancel(NOTIFICATION_ID)
                        stopForeground(false)
                        stopSelf()
                        return@runBlocking
                    }
                    startActivity(activityIntent)
                } else {
                    NotificationManagerCompat.from(this@SenderService)
                        .cancel(NOTIFICATION_ID)
                    stopForeground(false)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onHandleWork(intent: Intent) {

    }
}