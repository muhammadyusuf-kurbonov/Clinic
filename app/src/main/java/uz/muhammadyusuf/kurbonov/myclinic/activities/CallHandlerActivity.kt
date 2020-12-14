package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.Bus
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ToastViewBinding
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.MainViewModel
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates


class CallHandlerActivity : AppCompatActivity() {

    private val model by viewModel<MainViewModel>()
    private lateinit var binding: ToastViewBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ToastViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("Activity recreated")

        val string = intent.extras?.getString(EXTRA_PHONE_NUMBER)
        model.searchInDatabase(string ?: "")


        lifecycleScope.launch {
            launch {
                model.searchResult.collect {
                    when (it) {
                        is SearchStates.Loading -> {
                            binding.tvName.text = "Searching ..."
                            binding.btnCard.isEnabled = false
                        }
                        is SearchStates.Found -> {
                            binding.btnCard.isEnabled = true
                            binding.tvName.text = "It's ${it.contact.name}"

                            Toast.makeText(
                                this@CallHandlerActivity,
                                "It's patient",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is SearchStates.Error -> TODO()
                        SearchStates.NotFound -> {
                            binding.tvName.text = "Searching ..."
                            binding.btnCard.isEnabled = false
                        }
                    }
                }

                delay(2000)
                recreate()
            }
        }

        model.viewModelScope.launch {
            Bus.state.collect {
                if (it == TelephonyManager.EXTRA_STATE_IDLE) {
                    delay(1500)
                    Timber.d("Finish")
                    finish()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            while (true) {
                delay(500)
                binding.root.requestFocus()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager).moveTaskToFront(
            taskId, ActivityManager.MOVE_TASK_NO_USER_ACTION
        )
    }
}