package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.annotation.SuppressLint
import android.content.Intent.EXTRA_PHONE_NUMBER
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import uz.muhammadyusuf.kurbonov.myclinic.Bus
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ToastViewBinding
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.MainViewModel
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

class CallHandlerActivity : AppCompatActivity() {

    private val model by viewModel<MainViewModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ToastViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model.searchInDatabase(intent.extras?.getString(EXTRA_PHONE_NUMBER) ?: "")

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
            }

            Bus.state.collect {
                if (it == TelephonyManager.EXTRA_STATE_IDLE)
                    finish()
            }
        }


    }
}