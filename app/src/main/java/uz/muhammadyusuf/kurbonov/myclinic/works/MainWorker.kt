package uz.muhammadyusuf.kurbonov.myclinic.works

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
) {

    private var isActive = true
    private val notificationID = 15

    override suspend fun doWork(): Result {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        setForeground(ForegroundInfo(notificationID, getNotificationTemplate().build()))

        App.appViewModel.state.collect { state ->
            when (state) {
                State.Loading -> changeNotificationMessage(R.string.searching_text)
                State.Finished -> deactivateWorker()


                State.AuthRequest -> createAuthRequestNotification()


                State.ConnectionError -> changeNotificationMessage(R.string.no_connection)
                is State.Error -> changeNotificationMessage(R.string.unknown_error)


                State.NotFound -> changeNotificationMessage(R.string.not_found)
                is State.Found -> createCustomerInfoNotification(state.customer)
            }
        }

        // Keep worker live
        while (isActive) {
            // cycle
        }

        return Result.success()
    }

    private fun createAuthRequestNotification() {
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationID, getNotificationTemplate().apply {

                setContentIntent(getAuthActivityIntent())

                setContentText(applicationContext.getText(R.string.auth_text))

            }.build())
    }

    private fun createCustomerInfoNotification(customer: Customer) {
        printToConsole(customer.toString())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "32desk_notification_channel",
            "Notifications of app 32Desk.com",
            NotificationManager.IMPORTANCE_HIGH
        )
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)

    }

    private fun changeNotificationMessage(msg: String) {
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationID, getNotificationTemplate().apply {
                setContentText(msg)
            }.build())
    }

    private fun changeNotificationMessage(msgStringId: Int) {
        changeNotificationMessage(applicationContext.getString(msgStringId))
    }

    private fun deactivateWorker() {
        NotificationManagerCompat.from(applicationContext)
            .cancelAll()

        // must be last
        isActive = false
    }

    private fun getAuthActivityIntent() = PendingIntent.getActivity(
        applicationContext,
        111,
        Intent(applicationContext, LoginActivity::class.java).apply {
            putExtra("uz.muhammadyusuf.kurbonov.myclinic.phone", DataHolder.phoneNumber)
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT
        else 0
    )

    private fun getNotificationTemplate() =
        NotificationCompat.Builder(applicationContext, "clinic_info")
            .apply {

                setChannelId("32desk_notification_channel")

                setContentTitle(applicationContext.getString(R.string.app_name))

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setOnlyAlertOnce(true)

                setAutoCancel(true)
            }


    private fun printToConsole(msg: String) {
        Timber.d(msg)
    }
}