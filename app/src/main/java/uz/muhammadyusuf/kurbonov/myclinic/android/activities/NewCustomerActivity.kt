package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.coroutines.launch
import timber.log.Timber
import uz.muhammadyusuf.kurbonov.myclinic.App
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.api.customers.CustomerAddRequestBody
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.databinding.ActivityNewCustomerBinding
import uz.muhammadyusuf.kurbonov.myclinic.di.API
import uz.muhammadyusuf.kurbonov.myclinic.utils.attempts
import java.util.*

class NewCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCustomerBinding

    private var phoneFieldFormatted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App.getAppViewModelInstance().reduce(Action.Finish)

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
                val api = API.getAPIService()

                val response = attempts(10) {
                    api.addCustomer(
                        CustomerAddRequestBody(
                            first_name = binding.edFirstName.text.toString(),
                            last_name = binding.edLastName.text.toString(),
                            phone = binding.edPhone.text.toString()
                        )
                    )

                }
                if (response.isSuccessful) {
                    Timber.d("Successful added user")
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.new_customer_toast, binding.edFirstName.text.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                    App.getAppViewModelInstance().reduce(Action.Finish)
                    finish()
                } else {
                    Timber.e(IllegalStateException(response.errorBody()?.charStream()?.readText()))
                    binding.btnOk.isEnabled = true
                    binding.btnOk.text = getString(android.R.string.ok)
                }
            }
        }

    }
}