package uz.muhammadyusuf.kurbonov.myclinic.android.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import uz.muhammadyusuf.kurbonov.myclinic.api.toContact
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.view.OverlayCompose
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.utils.NetworkIOException
import uz.muhammadyusuf.kurbonov.myclinic.utils.retries
import java.io.IOException

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val customer = retries(10) {
                DI.getAPIService().searchCustomer("+998903500490")
            }.body()!!.toContact()
            setContent {
                Column {
                    var state by remember {
                        mutableStateOf<State>(State.None)
                    }
                    OverlayCompose(state = state)

                    @Suppress("ThrowableNotThrown") val states = arrayListOf(
                            State.Started,
                            State.Finished,
                            State.Found(customer, CallDirection.INCOME),
                            State.NoConnectionState,
                            State.NotFound,
                            State.None,
                            State.Searching,
                            State.TooSlowConnectionError,
                            State.AddNewCustomerRequest("+998913975538"),
                            State.AuthRequest("+998913975538"),
                            State.Error(NetworkIOException(IOException())),
                            State.PurposeRequest(customer, "1158498494984"),
                    )
                    LazyRow {
                        items(states) {
                            Button(onClick = { state = it }) {
                                Text(text = it.javaClass.simpleName)
                            }
                        }
                    }
                }
            }
        }
    }
}
