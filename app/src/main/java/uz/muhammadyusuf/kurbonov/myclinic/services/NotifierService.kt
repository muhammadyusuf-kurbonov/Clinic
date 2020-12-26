package uz.muhammadyusuf.kurbonov.myclinic.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.AppEvent
import uz.muhammadyusuf.kurbonov.myclinic.eventbus.EventBus
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
import uz.muhammadyusuf.kurbonov.myclinic.network.user_search.UserSearchService
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.EXTRA_PHONE
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver.Companion.NOTIFICATION_ID
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NotifierService : JobIntentService() {

    private val searchService: UserSearchService by inject()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)


    override fun onHandleWork(intent: Intent) {
        Timber.tag("lifecycle").d("onHandle work start")

    }

    private lateinit var phoneNumber: String

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("lifecycle").d("Destroyed")
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
        view.setTextViewText(R.id.tvName, "Searching ...")
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification.build())

        serviceScope.launch {
            EventBus.event.collect {
                if (it is AppEvent.StopServiceEvent) {
                    NotificationManagerCompat.from(this@NotifierService)
                        .cancel(NOTIFICATION_ID)
                    stopForeground(true)
                    stopSelf()
                    serviceJob.cancel()
                    Timber.d("Service job complete")
                }
            }
        }

        var states: SearchStates = SearchStates.Loading

        runBlocking(serviceScope.coroutineContext + Dispatchers.IO) {
            try {
                withTimeout(10000) {
                    val user = searchService.searchUser(phoneNumber)
                    Timber.d("user is $user")

                    if (user.code == "404")
                        states = SearchStates.NotFound
                    else if (user.code == "200")
                        states = SearchStates.Found(user.toContact()!!)
                }
            } catch (e: Exception) {
                states = SearchStates.Error(e)
            }

            Timber.d("state is $states")
            view.setTextViewText(
                R.id.tvName, when (states) {
                    is SearchStates.Loading -> "Searching ..."
                    is SearchStates.Found -> (states as SearchStates.Found).contact.name
                    is SearchStates.Error -> {
                        val exception = (states as SearchStates.Error).exception
                        Timber.e(exception)
                        when (exception) {
                            is TimeoutCancellationException, is SocketTimeoutException -> "Too slow internet"
                            is UnknownHostException -> "No internet connection"
                            else -> "Sorry, error occurred!"
                        }
                    }
                    SearchStates.NotFound -> "Nobody was found"
                }
            )

            if (states is SearchStates.Found) {
                with(view) {
                    val found = states as SearchStates.Found
                    setTextViewText(R.id.tvPhone, (found).contact.phoneNumber)
                    setTextViewText(R.id.tvBalance, "$ 400")
                    setTextViewText(
                        R.id.tvAddress,
                        "Home address: " + (found.contact.address?.toString() ?: "N/A")
                    )
                    setTextViewText(
                        R.id.tvJob,
                        "Working company: " + (found.contact.company?.name ?: "N/A")
                    )
                }
            } else {
                with(view) {
                    setTextViewText(R.id.tvPhone, phoneNumber)
                    setTextViewText(
                        R.id.tvAddress,
                        "N/A"
                    )
                    setTextViewText(
                        R.id.tvJob,
                        "N/A"
                    )
                }
            }

            notification.setContent(view)
            notification.setCustomBigContentView(view)
            NotificationManagerCompat.from(this@NotifierService)
                .notify(NOTIFICATION_ID, notification.build())

            Timber.tag("lifecycle").d("Work stop")
        }

        @Suppress("UNREACHABLE_CODE")
        return super.onStartCommand(intent, flags, startId)
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