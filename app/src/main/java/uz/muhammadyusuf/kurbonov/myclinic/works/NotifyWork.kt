package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.squareup.picasso.Picasso
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.recievers.CallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.utils.getBaseNotification
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder.phoneNumber
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NotifyWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val view = RemoteViews(context.packageName, R.layout.notification_view)
        val notification = getBaseNotification(context)

        val states = DataHolder.searchState

        when (DataHolder.type) {
            CallDirection.INCOME -> view.setImageViewResource(
                R.id.imgType,
                R.drawable.ic_baseline_phone_in_24
            )
            CallDirection.OUTGOING -> view.setImageViewResource(
                R.id.imgType,
                R.drawable.ic_phone_outgoing
            )
        }

        view.setTextViewText(
            R.id.tvName, when (states) {
                is State.Loading -> context.getString(R.string.searching_text)
                is State.Found -> states.customer.name
                is State.Error -> {
                    val exception = states.exception
                    Timber.e(exception)
                    when (exception) {
                        is TimeoutCancellationException, is SocketTimeoutException -> context.getString(
                            R.string.too_slow
                        )
                        is UnknownHostException -> context.getString(R.string.no_connection)
                        else -> context.getString(R.string.unknown_error)
                    }
                }
//                SearchStates.ConnectionError -> context.getString(R.string.connection_error)
                State.NotFound -> context.getString(R.string.not_found)
                State.AuthRequest -> context.getString(R.string.auth_text)
                else -> ""
            }
        )

        if (states is State.Found) {
            with(view) {
                val contact = states.customer
                setTextViewText(R.id.tvPhone, contact.phoneNumber)
                setTextViewText(
                    R.id.tvBalance,
                    context.getString(R.string.balance) + contact.balance
                )
                try {
                    setImageViewBitmap(
                        R.id.imgAvatar,
                        Picasso.get().load(contact.avatarLink).get()
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                }

                val lastAppointmentText = if (contact.lastAppointment != null) {
                    val lastAppointment = contact.lastAppointment!!
                    "${lastAppointment.date} - ${lastAppointment.doctor?.name ?: ""} - ${lastAppointment.diagnosys}"
                } else context.getString(R.string.not_avaible)

                setTextViewText(R.id.tvLastVisit, lastAppointmentText)
                val nextAppointmentText = if (contact.nextAppointment != null) {
                    val nextAppointment = contact.nextAppointment!!
                    "${
                        nextAppointment.date
                    } - ${nextAppointment.doctor} - ${nextAppointment.diagnosys}"
                } else context.getString(R.string.not_avaible)

                setTextViewText(R.id.tvNextVisit, nextAppointmentText)
            }
        } else {
            with(view) {
                setTextViewText(R.id.tvPhone, phoneNumber)
                setTextViewText(
                    R.id.tvLastVisit,
                    context.getString(R.string.not_avaible)
                )
            }
        }


        notification.setContent(view)

        notification.setContent(view)
        notification.setCustomBigContentView(view)
        NotificationManagerCompat.from(context)
            .notify(CallReceiver.NOTIFICATION_ID, notification.build())

        return Result.success()
    }
}