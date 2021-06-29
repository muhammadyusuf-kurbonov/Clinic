package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.AppViewModelProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalAnimationApi
@Composable
fun OverlayScreen() {
    var isExpanded by remember {
        mutableStateOf(true)
    }
    Row(verticalAlignment = Alignment.Bottom) {
        IconButton(onClick = {
            isExpanded = !isExpanded
            println("expanded is $isExpanded")
        }) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(1.dp, MaterialTheme.colors.primary, shape = CircleShape)
                    .padding(8.dp)
            )

        }
        Spacer(modifier = Modifier.width(4.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colors.primary, shape = RoundedCornerShape(8.dp))
        ) {

            val appViewModel = AppViewModelProvider.current

            val authState by appViewModel.authState.collectAsState()
            val customerState by appViewModel.customerState.collectAsState()

            Box(Modifier.padding(4.dp)) {
                OverlayContent(authState = authState, customerState = customerState)
            }
        }
    }
}

@Composable
fun OverlayContent(
    authState: AuthState,
    customerState: CustomerState
) {
    if ((authState is AuthState.ConnectionFailed) or
        (customerState is CustomerState.ConnectionFailed)
    ) {
        Column {
            Text(text = stringResource(id = R.string.no_connection))
            Spacer(modifier = Modifier.height(2.dp))
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Retry")
            }
        }
        return
    }

    if (authState !is AuthState.AuthSuccess) {
        // todo: start login page and retry
    }

    when (customerState) {
        CustomerState.ConnectionFailed -> {
        } // implemented before
        CustomerState.Default -> {
            Text(text = "Welcome")
        }
        is CustomerState.Found -> CustomerInfo(customer = customerState.customer)
        CustomerState.NotFound -> {
            Text(text = stringResource(id = R.string.not_found))
        }
    }
}

@Composable
fun CustomerInfo(customer: Customer) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val nameAndPhone = buildAnnotatedString {

                withStyle(MaterialTheme.typography.h6.toSpanStyle()) {
                    append(customer.last_name + " " + customer.first_name + "\n")
                }

                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colors.onBackground
                            .copy(alpha = ContentAlpha.disabled)
                    )
                ) {
                    append(customer.phone)
                }
            }
            Text(
                modifier = Modifier
                    .padding(4.dp),
                text = nameAndPhone,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = stringResource(id = R.string.balance, customer.balance),
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            Column(modifier = Modifier.weight(4f)) {
                Text(
                    text = stringResource(id = R.string.next_appointment),
                    fontWeight = FontWeight.Bold
                )
                if (customer.nextAppointment != null) {
                    val nextAppointment = customer.nextAppointment!!
                    val lastAppointmentText =
                        "${simpleDateFormat.format(nextAppointment.date)} \n" +
                                "${nextAppointment.user}\n" +
                                nextAppointment.diagnose
                    Text(text = lastAppointmentText, modifier = Modifier.padding(4.dp))
                } else {
                    Text(
                        text = "There isn't coming appointment yet",
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.previous_appointment),
                    fontWeight = FontWeight.Bold
                )
                if (customer.lastAppointment != null) {
                    val lastAppointment = customer.lastAppointment!!
                    val lastAppointmentText =
                        "${simpleDateFormat.format(lastAppointment.date)}\n" +
                                "${lastAppointment.user}\n" +
                                lastAppointment.diagnose
                    Text(text = lastAppointmentText, modifier = Modifier.padding(4.dp))
                } else {
                    Text(
                        text = "No previous appointment yet", modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            val painter = rememberCoilPainter(
                request = customer.avatar_url,
                fadeIn = true
            )
            when (painter.loadState) {
                is ImageLoadState.Success -> {
                    Image(
                        modifier = Modifier.weight(1f),
                        painter = painter,
                        contentDescription = "",
                    )
                }

                else -> Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(id = R.drawable.ic_action_avatar_placeholder),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                )
            }
        }
    }
}