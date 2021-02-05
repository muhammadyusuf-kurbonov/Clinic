package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.work.*
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityExplainBinding
import uz.muhammadyusuf.kurbonov.myclinic.model.CommunicationDataHolder
import uz.muhammadyusuf.kurbonov.myclinic.services.SenderService
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest

class ExplainActivity : AppCompatActivity() {

    private var isSent = false

    private lateinit var phone: String
    private lateinit var status: String
    private var duration: Long = 0
    private lateinit var type: String

    private lateinit var binding: ActivityExplainBinding
    private var note: String = ""
    private lateinit var holder: CommunicationDataHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        Timber.d("${intent.extras}")

        holder = intent.getParcelableExtra("data") ?: throw IllegalStateException()
        phone = holder.phone
        status = holder.status
        duration = holder.duration
        type = holder.type


        binding.rgCause.setOnCheckedChangeListener { _, checkedId ->
            binding.edOther.isVisible = checkedId == R.id.rbOther
        }

        NotificationManagerCompat.from(applicationContext)
            .cancelAll()
        stopService(Intent(this, SenderService::class.java))

        binding.btnSend.setOnClickListener {

            note = when (binding.rgCause.checkedRadioButtonId) {
                R.id.rbOther -> {
                    binding.edOther.text.toString()
                }
                else -> binding.rgCause.findViewById<RadioButton>(
                    binding.rgCause.checkedRadioButtonId
                ).text.toString()
            }

            isSent = true

            val data = Data.Builder()
            data.putString(SendStatusRequest.INPUT_PHONE, phone)
            data.putString(SendStatusRequest.INPUT_STATUS, status)
            data.putLong(
                SendStatusRequest.INPUT_DURATION,
                duration
            )
            data.putString(SendStatusRequest.INPUT_NOTE, note)
            data.putString(SendStatusRequest.INPUT_TYPE, type)
            val constraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
                .setInputData(data.build())
                .setConstraints(constraint)
                .addTag("sender")
                .build()
            WorkManager.getInstance(this@ExplainActivity)
                .enqueueUniqueWork(
                    "sender",
                    ExistingWorkPolicy.REPLACE,
                    request
                )

            finish()
        }
    }


    override fun onBackPressed() {
    }

    override fun onStop() {
        super.onStop()
        if (!isSent)
            startActivity(Intent(this, ExplainActivity::class.java).apply {
                putExtra("data", holder)
            })
    }
}