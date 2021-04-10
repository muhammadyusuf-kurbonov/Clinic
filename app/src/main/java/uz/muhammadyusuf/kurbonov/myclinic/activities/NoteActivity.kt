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
import uz.muhammadyusuf.kurbonov.myclinic.works.NoteInsertWork

class NoteActivity : AppCompatActivity() {

    private var isSent = false

    private var note: String = ""

    private lateinit var binding: ActivityExplainBinding
    private var communicationId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())

        Timber.d("${intent.extras}")

        communicationId = intent.extras?.getString("communicationId")
            ?: throw IllegalStateException("No id specified ${intent.extras}")

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


            val constraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequestBuilder<NoteInsertWork>()
                    .setInputData(
                        workDataOf(
                            "id" to communicationId,
                            "body" to note
                        )
                    )
                    .setConstraints(constraint)
                    .build()
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
                putExtra("communicationId", communicationId)
            })
    }
}