package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.toContact
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder.phoneNumber

class SearchWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        var states: SearchStates = SearchStates.Loading
        runBlocking {

            try {
                val searchService by inject(APIService::class.java)
                withTimeout(10000) {
                    val customer =
                        searchService.searchCustomer(phoneNumber, withAppointments = 0)
                    Timber.d("user is $customer")

                    when {
                        customer.code() == 404 -> states = SearchStates.NotFound
                        customer.code() == 401 -> states = SearchStates.AuthRequest
                        customer.code() == 407 -> states = SearchStates.ConnectionError
                        customer.code() == 200 -> states =
                            if (customer.body()!!.data.isNotEmpty()) {
                                SearchStates.Found(customer.body()!!.toContact())
                            } else {
                                SearchStates.NotFound
                            }
                    }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                states = SearchStates.Error(e)
            }
        }
        DataHolder.searchState = states

        return if (states is SearchStates.Error)
            Result.failure()
        else Result.success()
    }

}