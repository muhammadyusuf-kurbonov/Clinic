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
import androidx.work.ExperimentalExpeditedWork
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.NewCustomerActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State

class MainWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
    appContext,
    params,
) {

    private var isActive = true
        set(value) {
            printToConsole("activated changed: new value $value")
            field = value
        }
    private val notificationID = 100

    @ExperimentalExpeditedWork
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(notificationID, getNotificationTemplate().build())
    }

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        isActive = true
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            App.appViewModel.state.collect { state ->
                printToConsole("received state $state")
                when (state) {
                    State.Loading -> changeNotificationMessage(R.string.searching_text)
                    State.Finished -> deactivateWorker()

                    is State.AuthRequest -> createAuthRequestNotification(state.phone)
                    is State.AddNewCustomerRequest -> createAddCustomerNotification(state.phone)

                    State.ConnectionError -> changeNotificationMessage(R.string.no_connection)
                    State.TooSlowConnectionError -> changeNotificationMessage(R.string.too_slow)
                    is State.Error -> {
                        FirebaseCrashlytics.getInstance().recordException(state.exception)
                        changeNotificationMessage(R.string.unknown_error)
                    }


                    State.NotFound -> changeNotificationMessage(R.string.not_found)
                    is State.Found -> createCustomerInfoNotification(state.customer)
                    is State.CommunicationInfoSent -> createPurposeSelectionNotification(
                        state.customer,
                        state._id
                    )
                }
            }
        }

        printToConsole("new scope created and launcher")

        // Keep worker live
        while (isActive) {
            // cycle
        }

        printToConsole("Work done!")
        return Result.success()
    }

    private fun createPurposeSelectionNotification(customer: Customer, _id: String) {
        val activityIntent = Intent(applicationContext, NoteActivity::class.java).apply {
            putExtra(
                "communicationId", _id
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }


        val notification = getNotificationTemplate().apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setOngoing(true)
            setContentText(applicationContext.getString(R.string.purpose_msg, customer.name))
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    activityIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
                )
            )
            setAutoCancel(true)
        }.build()
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationID, notification)
    }

    private fun createAddCustomerNotification(phone: String) {
        printToConsole("new user request")
        val notification = NotificationCompat.Builder(applicationContext, "action_request")
            .apply {
                setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        Intent(applicationContext, NewCustomerActivity::class.java).apply {
                            putExtra("phone", phone)
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
                        phone
                    )
                )
            }

        NotificationManagerCompat.from(applicationContext)
            .notify(notificationID, notification.build())
    }

    private fun createAuthRequestNotification(phone: String) {
        NotificationManagerCompat.from(applicationContext)
            .notify(notificationID, getNotificationTemplate().apply {

                setContentIntent(getAuthActivityIntent(phone))

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
            .notify(notificationID, notification.build())

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
        printToConsole("deactivation ...")
        // must be last
        isActive = false
    }

    private fun getAuthActivityIntent(phone: String) = PendingIntent.getActivity(
        applicationContext,
        111,
        Intent(applicationContext, LoginActivity::class.java).apply {
            putExtra("uz.muhammadyusuf.kurbonov.myclinic.phone", phone)
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
        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())
        Timber.tag("main_worker").d(msg)
    }
}