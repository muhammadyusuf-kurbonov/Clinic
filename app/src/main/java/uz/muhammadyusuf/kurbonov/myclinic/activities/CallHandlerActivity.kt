package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.databinding.CallHandlerActivityBinding
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.CallStateListener
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.MainViewModel
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

class CallHandlerActivity : AppCompatActivity() {

    private val model by viewModel<MainViewModel>()

    private val callStateListener = CallStateListener {
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = CallHandlerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras?.containsKey(EXTRA_PHONE_NUMBER) == true) {
            val phoneNumber =
                intent.extras?.getCharSequence(EXTRA_PHONE_NUMBER).toString()
            binding.tvNumber.text = phoneNumber

            model.searchResult.observe(this) {
                when (it) {
                    is SearchStates.Loading -> {
                        binding.tvNumber.text = "Loading ..."
                    }
                    is SearchStates.NotFound -> {
                        Timber.d("Not found")
                        Toast.makeText(this, "It is not clinic's patient", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                    is SearchStates.Error -> {
                        Toast.makeText(
                            this,
                            "Error occurred. ${it.exception.localizedMessage}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        finish()
                    }
                    is SearchStates.Found -> {
                        binding.tvNumber.text = it.contact.name
                    }
                }
            }
            model.searchInDatabase(phoneNumber)
        }
        requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 241)

        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        manager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Thank you", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        const val EXTRA_PHONE_NUMBER = "phone"
        fun start(context: Context, phone: String) {
            Intent(context, CallHandlerActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(EXTRA_PHONE_NUMBER, phone)
                .let(context::startActivity)
        }
    }
}