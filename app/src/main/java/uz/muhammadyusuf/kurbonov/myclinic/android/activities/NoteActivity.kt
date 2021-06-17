package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityExplainBinding
import uz.muhammadyusuf.kurbonov.myclinic.utils.initTimber

class NoteActivity : AppCompatActivity() {

    private var isSent = false

    private var note: String = ""

    private lateinit var binding: ActivityExplainBinding
    private var communicationId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTimber()

        communicationId = intent.extras?.getString("communicationId")
            ?: throw IllegalStateException("No id specified ${intent.extras}")

        binding.rgCause.setOnCheckedChangeListener { _, checkedId ->
            binding.edOther.isVisible = checkedId == R.id.rbOther
        }

        App.actionBus.value = Action.Finish
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