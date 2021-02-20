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
import uz.muhammadyusuf.kurbonov.myclinic.works.SendStatusRequest

class NoteActivity : AppCompatActivity() {

    private var isSent = false

    private var note: String = ""

    private lateinit var binding: ActivityExplainBinding
    private lateinit var holder: CommunicationDataHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        Timber.d("${intent.extras}")

        holder = intent.getParcelableExtra("data") ?: throw IllegalStateException()

        binding.rgCause.setOnCheckedChangeListener { _, checkedId ->
            binding.edOther.isVisible = checkedId == R.id.rbOther
        }

        NotificationManagerCompat.from(applicationContext)
            .cancelAll()

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
            data.putString(SendStatusRequest.INPUT_PHONE, holder.phone)
            data.putString(SendStatusRequest.INPUT_STATUS, holder.status)
            data.putLong(
                SendStatusRequest.INPUT_DURATION,
                holder.duration
            )
            data.putString(SendStatusRequest.INPUT_NOTE, note)
            data.putString(SendStatusRequest.INPUT_TYPE, holder.type)

            val constraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SendStatusRequest>()
                .setInputData(data.build())
                .setConstraints(constraint)
                .addTag("sender")
                .build()

            WorkManager.getInstance(this@NoteActivity)
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
            startActivity(Intent(this, NoteActivity::class.java).apply {
                putExtra("data", holder)
            })
    }
}