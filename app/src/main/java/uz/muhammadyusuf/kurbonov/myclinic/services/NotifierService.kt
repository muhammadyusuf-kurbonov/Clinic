package uz.muhammadyusuf.kurbonov.myclinic.services

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
import uz.muhammadyusuf.kurbonov.myclinic.EventBus
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.network.UserSearchService
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
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
        if (!intent.extras?.getString(EXTRA_PHONE, "").isNullOrEmpty())
            phoneNumber = intent.extras?.getString(EXTRA_PHONE) ?: throw IllegalArgumentException()
        else
            return
        val view = RemoteViews(packageName, R.layout.toast_view)
        val notification = NotificationCompat.Builder(this, "clinic_info")
            .apply {
                setContent(view)

                setSmallIcon(R.drawable.ic_launcher_foreground)

                setOngoing(true)

                setOnlyAlertOnce(true)

                val pendingIntent = PendingIntent.getActivity(
                    this@NotifierService,
                    0,
                    Intent(this@NotifierService, MainActivity::class.java),
                    0
                )

//                addAction(R.drawable.ic_baseline_assignment_ind_24, "Open card", pendingIntent)

                view.setOnClickPendingIntent(R.id.btnCard, pendingIntent)
                priority = NotificationCompat.PRIORITY_MAX
            }

        startForeground(NOTIFICATION_ID, notification.build())
        view.setTextViewText(R.id.tvName, "Searching ...")
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification.build())

        serviceScope.launch {
            EventBus.event.collect {
                if (it == 1) {
                    NotificationManagerCompat.from(this@NotifierService)
                        .cancel(NOTIFICATION_ID)
                    serviceJob.cancel()
                    stopForeground(true)
                    stopSelf()
                    Timber.d("Service job complete")
                }
            }
        }

        var states: SearchStates = SearchStates.Loading

        runBlocking {
            try {
                withTimeout(10000) {
                    val user = searchService.searchUser(phoneNumber).execute()
                    Timber.d("user is ${user.body()}")
                    if (user.isSuccessful) {
                        val body = user.body()!!
                        if (body.code == "404")
                            states = SearchStates.NotFound
                        else if (body.code == "200")
                            states = SearchStates.Found(body.toContact()!!)
                    }
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

            notification.setContent(view)
            NotificationManagerCompat.from(this@NotifierService)
                .notify(NOTIFICATION_ID, notification.build())

            Timber.tag("lifecycle").d("onHandleWork stop")
            awaitCancellation()
        }
    }

    private lateinit var phoneNumber: String

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("lifecycle").d("Destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag("lifecycle").d("Started")
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