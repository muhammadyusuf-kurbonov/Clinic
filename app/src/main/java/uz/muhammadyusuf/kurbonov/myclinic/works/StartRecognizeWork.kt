package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import android.widget.RemoteViews
import androidx.work.Worker
import androidx.work.WorkerParameters
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

class StartRecognizeWork(
    val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val INPUT_PHONE = "input.phone"
        const val INPUT_TYPE = "input.type"
    }

    override fun doWork(): Result {
        DataHolder.searchState = SearchStates.Loading
        val view = RemoteViews(context.packageName, R.layout.notification_view)
        val phoneNumber = workerParams.inputData.getString(INPUT_PHONE) ?: ""
        val type = workerParams.inputData.getString(INPUT_TYPE)

        DataHolder.phoneNumber = phoneNumber
        DataHolder.type = if (type == "incoming") CallTypes.INCOME else CallTypes.OUTCOME

        view.setTextViewText(R.id.tvName, context.getString(R.string.searching_text))
        return Result.success()
    }
}