package uz.muhammadyusuf.kurbonov.myclinic.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityNewUserBinding
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.customers.CustomerAddRequestBody
import uz.muhammadyusuf.kurbonov.myclinic.works.DataHolder
import java.util.*

class NewUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewUserBinding

    private var phoneFieldFormatted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras?.containsKey("phone") == true) {
            binding.edPhone.setText(intent.extras?.getString("phone") ?: "")
        }

        binding.edPhone.addTextChangedListener {
            if (phoneFieldFormatted)
                return@addTextChangedListener
            val instance = PhoneNumberUtil.createInstance(this)
            val currentNumber = try {
                instance.format(
                    instance.parse(it.toString(), Locale.getDefault().country),
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )
            } catch (e: Exception) {
                Timber.e(e)
                it.toString()
            }

            phoneFieldFormatted = true
            binding.edPhone.setText(currentNumber)

        }


        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnOk.setOnClickListener {

            if (binding.edFirstName.text.isNullOrEmpty()) {
                binding.edFirstName.error = getString(R.string.required_error)
                return@setOnClickListener
            }
            if (binding.edLastName.text.isNullOrEmpty()) {
                binding.edLastName.error = getString(R.string.required_error)
                return@setOnClickListener
            }
            if (binding.edPhone.text.isNullOrEmpty()) {
                binding.edPhone.error = getString(R.string.required_error)
                return@setOnClickListener
            }

            binding.btnOk.text = "..."

            binding.btnOk.isEnabled = false

            lifecycleScope.launch {
                val api = KoinJavaComponent.getKoin().get(APIService::class)

                val response = api.addCustomer(
                    CustomerAddRequestBody(
                        first_name = binding.edFirstName.text.toString(),
                        last_name = binding.edLastName.text.toString(),
                        phone = binding.edPhone.text.toString()
                    )
                )

                if (response.isSuccessful) {
                    Timber.d("Successful added user")
                    DataHolder.phoneNumber = binding.edPhone.text.toString().replace("() -", "")
                    //TODO("Search and show")
                    finish()
                } else {
                    FirebaseCrashlytics.getInstance().recordException(
                        IllegalStateException(response.body().toString())
                    )
                    binding.btnOk.isEnabled = true
                    binding.btnOk.text = getString(android.R.string.ok)
                }
            }
        }

    }
}