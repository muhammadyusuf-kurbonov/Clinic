package uz.muhammadyusuf.kurbonov.myclinic.android.views.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.picasso.Picasso
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NewCustomerActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.views.BaseView
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.shared.printToConsole
import kotlin.random.Random

class NotificationView(
    val context: Context, viewModel: AppViewModel
) : BaseView(viewModel) {
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
        printToConsole("new user request")
        val notification =
            NotificationCompat.Builder(context, App.HEADUP_NOTIFICATION_CHANNEL_ID)
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

                    setChannelId(App.HEADUP_NOTIFICATION_CHANNEL_ID)

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

                setContentText(context.getText(R.string.auth_request))
            }.build())
    }

    private fun createCustomerInfoNotification(customer: Customer, callDirection: CallDirection) {
        printToConsole(customer.toString())
        val view = RemoteViews(context.packageName, R.layout.customer_info)
        val notification =
            NotificationCompat.Builder(context, App.HEADUP_NOTIFICATION_CHANNEL_ID)
                .apply {

                    setContent(view)

                    setChannelId(App.HEADUP_NOTIFICATION_CHANNEL_ID)

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
                    R.drawable.ic_phone_in_24
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
                context.getString(R.string.balance, customer.balance)
            )

            try {
                setImageViewBitmap(R.id.imgAvatar, Picasso.get().load(customer.avatarLink).get())
            } catch (e: Exception) {
                e.printStackTrace()
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
        printToConsole("communicationId is $communicationId by creating notification")

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

    override suspend fun onCreate() {
        delay(1000)
    }

    override suspend fun onStateChange(state: State) {
        printToConsole("received state $state")
        when (state) {
            State.Started -> {
                changeNotificationMessage("")
            }
            State.Finished -> {
                onFinished()
                viewModel.coroutineScope.cancel()
            }

            State.Searching -> changeNotificationMessage(R.string.searching_text)
            is State.AuthRequest -> createAuthRequestNotification(state.phone)
            is State.AddNewCustomerRequest -> {
                createAddCustomerNotification(state.phone)
                viewModel.reduce(Action.Finish)
            }

            State.NoConnectionState -> changeNotificationMessage(R.string.no_connection)
            State.ConnectionTimeoutState -> changeNotificationMessage(R.string.read_timeout)

            is State.Error -> {
                Timber.e(state.exception)
                changeNotificationMessage(R.string.unknown_error)
            }

            State.NotFound -> changeNotificationMessage(R.string.not_found)
            is State.Found -> createCustomerInfoNotification(
                state.customer,
                state.callDirection
            )
            is State.PurposeRequest -> {
                createPurposeSelectionNotification(
                    state.customer,
                    state.communicationId
                )
                onFinished()
                viewModel.reduce(Action.Finish)
            }
            State.None -> {
                printToConsole("Worker started")
            }
        }
    }

    override fun onFinished() {
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
        NotificationCompat.Builder(context, App.HEADUP_NOTIFICATION_CHANNEL_ID)
            .apply {

                setChannelId(App.HEADUP_NOTIFICATION_CHANNEL_ID)

                setContentTitle(context.getString(R.string.app_name))

                setSmallIcon(R.drawable.ic_launcher_foreground)

                priority = NotificationCompat.PRIORITY_MAX

                setOngoing(false)

                setAutoCancel(true)
            }

}