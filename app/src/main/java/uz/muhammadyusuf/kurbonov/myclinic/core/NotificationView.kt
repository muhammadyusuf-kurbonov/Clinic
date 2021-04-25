package uz.muhammadyusuf.kurbonov.myclinic.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.NewCustomerActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.core.model.Customer
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.TAG_NOTIFICATIONS_VIEW
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber
import kotlin.random.Random

class NotificationView(
    val context: Context,
    private val stateFlow: StateFlow<State>
) {
    private val primaryNotificationID = 100
    private val secondaryNotificationID = 101

    private fun changeNotificationMessage(msg: String) {
        NotificationManagerCompat.from(context)
            .notify(primaryNotificationID, getNotificationTemplate().apply {
                setContentText(msg)
            }.build())
    }

    private fun changeNotificationMessage(msgStringId: Int) {
        changeNotificationMessage(context.getString(msgStringId))
    }

    private fun createAddCustomerNotification(phone: String) {
        log("new user request")
        val notification =
            NotificationCompat.Builder(context, "32desk_notification_channel")
                .apply {
                    setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            112,
                            Intent(context, NewCustomerActivity::class.java).apply {
                                putExtra("phone", phone)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                PendingIntent.FLAG_IMMUTABLE
                            else 0
                        )
                    )

                    setChannelId("32desk_notification_channel")

                    setSmallIcon(R.drawable.ic_launcher_foreground)

                    priority = NotificationCompat.PRIORITY_MAX

                    setOngoing(false)

                    setAutoCancel(true)

                    setContentText(
                        context.getString(
                            R.string.add_user_request,
                            phone
                        )
                    )
                }

        NotificationManagerCompat.from(context)
            .notify(secondaryNotificationID, notification.build())
    }

    private fun createAuthRequestNotification(phone: String) {
        NotificationManagerCompat.from(context)
            .notify(primaryNotificationID, getNotificationTemplate().apply {

                setContentIntent(getAuthActivityIntent(phone))

                setContentText(context.getText(R.string.auth_text))
            }.build())
    }

    private fun createCustomerInfoNotification(customer: Customer, callDirection: CallDirection) {
        log(customer.toString())
        val view = RemoteViews(context.packageName, R.layout.notification_view)
        val notification =
            NotificationCompat.Builder(context, "32desk_notification_channel")
                .apply {

                    setContent(view)

                    setChannelId("32desk_notification_channel")

                    setSmallIcon(R.drawable.ic_launcher_foreground)

                    priority = NotificationCompat.PRIORITY_MAX

                    setCustomBigContentView(view)

                    setAutoCancel(true)
                }

        with(view) {


            // Drawing icon for notification
            when (callDirection) {
                CallDirection.INCOME -> setImageViewResource(
                    R.id.imgType,
                    R.drawable.ic_baseline_phone_in_24
                )
                CallDirection.OUTGOING -> setImageViewResource(
                    R.id.imgType,
                    R.drawable.ic_phone_outgoing
                )
            }

            setTextViewText(R.id.tvName, customer.name)
            setTextViewText(R.id.tvPhone, customer.phoneNumber)

            setTextViewText(
                R.id.tvBalance,
                context.getString(R.string.balance) + customer.balance
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
        NotificationManagerCompat.from(context)
            .notify(primaryNotificationID, notification.build())

    }

    private fun createPurposeSelectionNotification(customer: Customer, communicationId: String) {
        log("communicationId is $communicationId by creating notification")

        val activityIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra(
                "communicationId", communicationId
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }


        val notification = getNotificationTemplate().apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setOngoing(false)
            setContentText(context.getString(R.string.purpose_msg, customer.name))
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    Random.nextInt(),
                    activityIntent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
                )
            )
            priority = NotificationCompat.PRIORITY_MAX
            setAutoCancel(true)
        }.build()

        NotificationManagerCompat.from(context)
            .notify(secondaryNotificationID, notification)
    }

    fun start() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            delay(1000)
            stateFlow.collect { state ->
                log("received state $state")
                when (state) {
                    State.Started -> {
                        changeNotificationMessage("")
                    }
                    State.Finished -> {
                        onFinished()
                        cancel()
                    }

                    State.Searching -> changeNotificationMessage(R.string.searching_text)
                    is State.AuthRequest -> createAuthRequestNotification(state.phone)
                    is State.AddNewCustomerRequest -> {
                        createAddCustomerNotification(state.phone)
                        onFinished()
                    }

                    State.ConnectionError -> changeNotificationMessage(R.string.no_connection)
                    State.TooSlowConnectionError -> changeNotificationMessage(R.string.too_slow)

                    is State.Error -> {
                        FirebaseCrashlytics.getInstance().recordException(state.exception)
                        changeNotificationMessage(R.string.unknown_error)
                    }

                    State.NotFound -> changeNotificationMessage(R.string.not_found)
                    is State.Found -> createCustomerInfoNotification(
                        state.customer,
                        state.callDirection
                    )
                    is State.CommunicationInfoSent -> {
                        createPurposeSelectionNotification(
                            state.customer,
                            state.communicationId
                        )

                        onFinished()
                    }
                    State.None -> {
                        log("Worker started")
                    }
                }
            }
        }
    }

    private fun getAuthActivityIntent(phone: String) = PendingIntent.getActivity(
        context,
        111,
        Intent(context, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_PHONE, phone)
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT
        else 0
    )

    private fun getNotificationTemplate() =
        NotificationCompat.Builder(context, "32desk_notification_channel")
            .apply {

                setChannelId("32desk_notification_channel")

                setContentTitle(context.getString(R.string.app_name))

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setAutoCancel(true)
            }

    private fun log(msg: String) {
        initTimber()
        Timber.tag(TAG_NOTIFICATIONS_VIEW).d(msg)
    }

    var onFinished: () -> Unit = {}
}