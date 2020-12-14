package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.databinding.CallHandlerActivityBinding
import uz.muhammadyusuf.kurbonov.myclinic.services.CallReceiver
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.MainViewModel
import uz.muhammadyusuf.kurbonov.myclinic.viewmodel.SearchStates

class CallHandlerActivity : AppCompatActivity() {

    private val model by inject<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = CallHandlerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras?.containsKey(CallReceiver.EXTRA_PHONE_NUMBER) == true) {
            val phoneNumber =
                intent.extras?.getCharSequence(CallReceiver.EXTRA_PHONE_NUMBER).toString()
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
                            "Error occured. ${it.exception.localizedMessage}",
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 241)
        }
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
}