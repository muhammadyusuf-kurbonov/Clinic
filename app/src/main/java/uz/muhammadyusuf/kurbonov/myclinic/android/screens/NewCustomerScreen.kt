package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.states.RegisterState

@Composable
fun NewCustomerScreen(
    finish: () -> Unit = {
    }
) {
    val controller = LocalAppControllerProvider.current
    val state by AppStateStore.registerState.collectAsState()
    NewCustomerForm(
        register = { firstName, lastName, phone ->
            controller.handle(Action.RegisterNewCustomer(firstName, lastName, phone))
        },
        state = state,
        finish = finish
    )
}

@Composable
fun NewCustomerForm(
    register: (firstName: String, lastName: String, phone: String) -> Unit,
    finish: () -> Unit = {},
    state: RegisterState
) {

    Column(modifier = Modifier.padding(4.dp)) {

        var firstName by remember {
            mutableStateOf("")
        }

        var lastName by remember {
            mutableStateOf("")
        }

        val customerPhoneNumber = LocalPhoneNumberProvider.current
        var phone by remember {
            mutableStateOf(customerPhoneNumber)
        }

        val phoneNumberUtil = PhoneNumberUtil.createInstance(LocalContext.current)

        val phoneFormatted = try {
            phoneNumberUtil.format(
                phoneNumberUtil.parse(
                    phone,
                    java.util.Locale.getDefault().country
                ), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )
        } catch (e: NumberParseException) {
            phone
        }

        Text(
            text = stringResource(
                id = R.string.register_new_patient,
                customerPhoneNumber
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            modifier = Modifier.padding(4.dp),
            value = firstName,
            onValueChange = { firstName = it },
            singleLine = true,
            isError = state == RegisterState.VerificationFailed
        )
        OutlinedTextField(
            modifier = Modifier.padding(4.dp),
            value = lastName,
            onValueChange = { lastName = it },
            singleLine = true,
            isError = state == RegisterState.VerificationFailed
        )
        OutlinedTextField(
            modifier = Modifier.padding(4.dp),
            value = phoneFormatted,
            onValueChange = { phone = it.replace("() -", "") },
            singleLine = true,
            isError = state == RegisterState.VerificationFailed,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            )
        )
        Row {
            Button(
                onClick = {
                    finish()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text(
                    text = stringResource(id = android.R.string.cancel),
                    style = MaterialTheme.typography.button
                )
            }

            val context = LocalContext.current
            Button(onClick = {
                try {
                    register(firstName, lastName, phone)
                    Toast.makeText(
                        context,
                        context.getString(R.string.new_customer_toast, firstName),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) {
                Text(text = "Register", style = MaterialTheme.typography.button)
            }
        }
    }
}

