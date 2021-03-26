package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.communications.CommunicationInfo
import uz.muhammadyusuf.kurbonov.myclinic.states.SearchStates
import uz.muhammadyusuf.kurbonov.myclinic.utils.PhoneCheckMismatchException
import uz.muhammadyusuf.kurbonov.myclinic.utils.getCallDetails
import uz.muhammadyusuf.kurbonov.myclinic.utils.maskPhoneNumber
import uz.muhammadyusuf.kurbonov.myclinic.utils.stopMonitoring
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder.type

class ReporterWork(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {

        Thread.sleep(1500)

        val callDetails = getCallDetails(context)

        val status = callDetails.status
        val duration = callDetails.duration

        stopMonitoring()
        NotificationManagerCompat.from(context).cancelAll()

        if (DataHolder.searchState is SearchStates.NotFound) {
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequest.from(NewUserAskWork::class.java)
            )
            return Result.success()
        }

        if (DataHolder.searchState !is SearchStates.Found)
            return Result.success()

        if (type == null) return Result.failure()

        if (DataHolder.phoneNumber.isEmpty()) return Result.success()


        if (DataHolder.phoneNumber != callDetails.phone) {

            val data = Data.Builder()
                .putString("phone from journal", callDetails.phone.maskPhoneNumber())
                .putString("required phone mask", DataHolder.phoneNumber.maskPhoneNumber())
                .build()

            FirebaseCrashlytics.getInstance().recordException(
                PhoneCheckMismatchException(callDetails.phone, DataHolder.phoneNumber)
            )

            return Result.failure(
                data
            )
        }

        val actualNumber = (DataHolder.searchState as SearchStates.Found).contact.phoneNumber
            .replace(" ", "")
        if (actualNumber != DataHolder.phoneNumber)
            throw IllegalStateException(
                "Searched and actual phone numbers are different" +
                        "$actualNumber - ${DataHolder.phoneNumber}"
            )

        return runBlocking {
            val apiService = KoinJavaComponent.get(APIService::class.java)
            NotificationManagerCompat.from(context)
                .cancelAll()
            val user = (DataHolder.searchState as SearchStates.Found).contact
            val communications =
                apiService.communications(
                    CommunicationInfo(
                        user.id,
                        status,
                        duration,
                        type!!.getAsString(),
                        body = ""
                    )
                )

            Timber.d("$communications")

            if (communications.isSuccessful) {
                DataHolder.communicationId = communications.body()!!._id
                WorkManager.getInstance(context)
                    .enqueue(
                        OneTimeWorkRequestBuilder<PurposeSelectorWork>().build()
                    )
                return@runBlocking Result.success()
            } else
                return@runBlocking Result.failure()
//
//                val data = Data.Builder()
//                data.putString(SendReportWork.INPUT_PHONE, DataHolder.phoneNumber)
//                data.putString(SendReportWork.INPUT_STATUS, status)
//                data.putLong(
//                    SendReportWork.INPUT_DURATION,
//                    duration
//                )
//                data.putString(SendReportWork.INPUT_NOTE, "")
//                data.putString(
//                    SendReportWork.INPUT_TYPE,
//                    if (DataHolder.type == CallTypes.INCOME) "incoming" else "outgoing"
//                )
//                val constraint = Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                    .build()
//                val request = OneTimeWorkRequestBuilder<SendReportWork>()
//                    .setInputData(data.build())
//                    .setConstraints(constraint)
//                    .addTag("sender")
//                    .build()
//                WorkManager.getInstance(context)
//                    .enqueueUniqueWork(
//                        "sender",
//                        ExistingWorkPolicy.REPLACE,
//                        request
//                    )

        }
    }
}