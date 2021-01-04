package uz.muhammadyusuf.kurbonov.myclinic.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.widget.RemoteViews
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.EXTRA_PHONE
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.NOTIFICATION_ID
import uz.muhammadyusuf.kurbonov.myclinic.utils.reformatDate
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NotifierService : JobIntentService() {

    private val searchService: APIService by inject()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)


    override fun onHandleWork(intent: Intent) {
        Timber.tag("lifecycle").d("onHandle work start")
    }

    private lateinit var phoneNumber: String

    override fun onDestroy() {
        NotificationManagerCompat.from(this@NotifierService)
            .cancel(NOTIFICATION_ID)
        stopForeground(false)
        serviceJob.cancel()
        Timber.tag("lifecycle").d("Destroyed")
        super.onDestroy()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag("lifecycle").d("Started")

        setTheme(R.style.Theme_MyClinic)

        if (!intent?.extras?.getString(EXTRA_PHONE, "").isNullOrEmpty())
            phoneNumber = intent?.extras?.getString(EXTRA_PHONE) ?: throw IllegalArgumentException()
        else
            return super.onStartCommand(intent, flags, startId)
        val view = RemoteViews(packageName, R.layout.notification_view)

        val notification = NotificationCompat.Builder(this, "clinic_info")
            .apply {

                setContent(view)

                setSmallIcon(R.drawable.ic_launcher_foreground)

                setOngoing(true)

                setCustomBigContentView(view)

                setAutoCancel(false)

                val pendingIntent = PendingIntent.getActivity(
                    this@NotifierService,
                    0,
                    Intent(this@NotifierService, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                view.setOnClickPendingIntent(R.id.btnCard, pendingIntent)
                priority = NotificationCompat.PRIORITY_MAX
            }
        startForeground(NOTIFICATION_ID, notification.build())
        view.setTextViewText(R.id.tvName, getString(R.string.searching_text))
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification.build())

        var states: SearchStates = SearchStates.Loading

        runBlocking(serviceScope.coroutineContext + Dispatchers.IO) {
            try {
                withTimeout(10000) {
                    val customer = searchService.searchCustomer(phoneNumber)
                    Timber.d("user is $customer")

                    when {
                        customer.code() == 404 -> states = SearchStates.NotFound
                        customer.code() == 401 -> states = SearchStates.AuthRequest
                        customer.code() == 200 -> states =
                            if (customer.body()!!.data.isNotEmpty()) {
                                SearchStates.Found(customer.body()!!.toContact())
                            } else {
                                SearchStates.NotFound
                            }
                    }
                }
            } catch (e: Exception) {
                states = SearchStates.Error(e)
            }

            if (states is SearchStates.AuthRequest)
                startActivity(Intent(this@NotifierService, LoginActivity::class.java).apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                })

            Timber.d("state is $states")
            view.setTextViewText(
                R.id.tvName, when (states) {
                    is SearchStates.Loading -> getString(R.string.searching_text)
                    is SearchStates.Found -> (states as SearchStates.Found).contact.name
                    is SearchStates.Error -> {
                        val exception = (states as SearchStates.Error).exception
                        Timber.e(exception)
                        when (exception) {
                            is TimeoutCancellationException, is SocketTimeoutException -> getString(
                                R.string.too_slow
                            )
                            is UnknownHostException -> getString(R.string.no_connection)
                            else -> getString(R.string.unknown_error)
                        }
                    }
                    SearchStates.NotFound -> getString(R.string.not_found)
                    SearchStates.AuthRequest -> getString(R.string.auth_text)
                }
            )

            if (states is SearchStates.Found) {
                with(view) {
                    val contact = (states as SearchStates.Found).contact
                    setTextViewText(R.id.tvPhone, contact.phoneNumber)
                    setTextViewText(R.id.tvBalance, getString(R.string.balance) + contact.balance)
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
                        "${lastAppointment.date} - ${lastAppointment.doctor.name} - ${lastAppointment.diagnosys}"
                    } else getString(R.string.not_avaible)

                    setTextViewText(R.id.tvLastVisit, lastAppointmentText)
                    val nextAppointmentText = if (contact.nextAppointment != null) {
                        val nextAppointment = contact.nextAppointment!!
                        "${
                            nextAppointment.date.reformatDate(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                "dd MMM yyyy HH:mm"
                            )
                        } - ${nextAppointment.doctor} - ${nextAppointment.diagnosys}"
                    } else getString(R.string.not_avaible)

                    setTextViewText(R.id.tvNextVisit, nextAppointmentText)
                }
            } else {
                with(view) {
                    setTextViewText(R.id.tvPhone, phoneNumber)
                    setTextViewText(
                        R.id.tvLastVisit,
                        getString(R.string.not_avaible)
                    )
                }
            }

            notification.setContent(view)
            notification.setCustomBigContentView(view)
            NotificationManagerCompat.from(this@NotifierService)
                .notify(NOTIFICATION_ID, notification.build())

            Timber.tag("lifecycle").d("Work stop")
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag("lifecycle").d("Created")
    }

    override fun onStopCurrentWork(): Boolean {
        Timber.tag("lifecycle").d("Stop Work")
        return super.onStopCurrentWork()
    }
}