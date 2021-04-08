package uz.muhammadyusuf.kurbonov.myclinic.works

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.NewUserActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.recievers.CallReceiver
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

        ForegroundInfo(notificationID, getNotificationTemplate().build())
        TODO("Replace with getForegroundInfo()")

        App.appViewModel.state.collect { state ->
            when (state) {
                State.Loading -> changeNotificationMessage(R.string.searching_text)
                State.Finished -> deactivateWorker()


                State.AuthRequest -> createAuthRequestNotification()
                is State.AddNewCustomerRequest -> createAddCustomerNotification()


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

    private fun createAddCustomerNotification() {
        val notification = NotificationCompat.Builder(applicationContext, "action_request")
            .apply {
                setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, NewUserActivity::class.java).apply {
                            putExtra("phone", DataHolder.phoneNumber)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        },
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0
                    )
                )

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setAutoCancel(true)

                setContentText(
                    applicationContext.getString(
                        R.string.add_user_request,
                        DataHolder.phoneNumber
                    )
                )
            }

        NotificationManagerCompat.from(applicationContext)
            .notify(CallReceiver.NOTIFICATION_ID, notification.build())
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
        val view = RemoteViews(applicationContext.packageName, R.layout.notification_view)
        val notification = NotificationCompat.Builder(applicationContext, "clinic_info")
            .apply {

                setContent(view)

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setCustomBigContentView(view)

                setAutoCancel(true)
            }

        when (App.appViewModel.callDirection) {
            CallDirection.INCOME -> view.setImageViewResource(
                R.id.imgType,
                R.drawable.ic_baseline_phone_in_24
            )
            CallDirection.OUTGOING -> view.setImageViewResource(
                R.id.imgType,
                R.drawable.ic_phone_outgoing
            )
        }

        view.setTextViewText(R.id.tvName, customer.name)

        with(view) {
            setTextViewText(R.id.tvPhone, customer.phoneNumber)

            setTextViewText(
                R.id.tvBalance,
                applicationContext.getString(R.string.balance) + customer.balance
            )

            try {
                setImageViewBitmap(R.id.imgAvatar, Picasso.get().load(customer.avatarLink).get())
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (customer.lastAppointment != null) {
                val lastAppointment = customer.lastAppointment!!
                val lastAppointmentText =
                    "${lastAppointment.date} - ${lastAppointment.doctor?.name ?: ""} - ${lastAppointment.diagnosys}"
                setTextViewText(R.id.tvLastVisit, lastAppointmentText)
            }

            if (customer.nextAppointment != null) {
                val nextAppointment = customer.nextAppointment!!
                val nextAppointmentText = "${
                    nextAppointment.date
                } - ${nextAppointment.doctor} - ${nextAppointment.diagnosys}"
                setTextViewText(R.id.tvNextVisit, nextAppointmentText)
            }
        }


        notification.setContent(view)
        notification.setCustomBigContentView(view)
        NotificationManagerCompat.from(applicationContext)
            .notify(CallReceiver.NOTIFICATION_ID, notification.build())

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