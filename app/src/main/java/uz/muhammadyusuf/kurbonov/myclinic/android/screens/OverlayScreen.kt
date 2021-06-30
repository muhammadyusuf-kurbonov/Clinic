package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.MainActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalPhoneNumberProvider
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.CustomerState
import uz.muhammadyusuf.kurbonov.myclinic.core.states.ReportState
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalAnimationApi
@Composable
fun OverlayScreen() {
    var isExpanded by remember {
        mutableStateOf(true)
    }
    Row(verticalAlignment = Alignment.Bottom) {

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .height(48.dp)
                .width(48.dp)
                .background(Color.White, shape = CircleShape)
                .border(1.dp, MaterialTheme.colors.primary, shape = CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            isExpanded = !isExpanded
                        }
                    )
                }
                .padding(8.dp)
        )


        Spacer(modifier = Modifier.width(4.dp))

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colors.primary, shape = RoundedCornerShape(8.dp))
        ) {

            val context = LocalContext.current
            val statesController = LocalAppControllerProvider.current
            val phoneNumber = LocalPhoneNumberProvider.current

            val authState by AppStateStore.authState.collectAsState()
            val customerState by AppStateStore.customerState.collectAsState()
            val reportState by AppStateStore.reportState.collectAsState()

            Box(Modifier.padding(4.dp)) {
                OverlayContent(authState = authState,
                    customerState = customerState,
                    reportState = reportState,
                    retry = {
                        statesController.handle(Action.Search(phoneNumber))
                    },
                    requestNewCustomerRegistration = {

                    }
                )
            }
        }
    }
}

@Composable
fun OverlayContent(
    authState: AuthState,
    customerState: CustomerState,
    reportState: ReportState,
    retry: () -> Unit = {},
    requestNewCustomerRegistration: () -> Unit = {},
    requestPurpose: () -> Unit = {},
) {
    if ((authState is AuthState.ConnectionFailed) or
        (customerState is CustomerState.ConnectionFailed)
    ) {
        SimpleActionButton(
            label = stringResource(id = R.string.no_internet_connection),
            buttonLabel = stringResource(id = R.string.retry)
        ) {
            retry()
        }
        return
    }

    if (authState !is AuthState.AuthSuccess) {

        val context = LocalContext.current

        LaunchedEffect(key1 = Unit) {
            context.startActivity(
                Intent(
                    context,
                    MainActivity::class.java
                )
                    .putExtra("route", "login")
                    .addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.auth_request),
                style = MaterialTheme.typography.subtitle2
            )
            Button(onClick = { retry() }) {
                Text(text = stringResource(R.string.retry), style = MaterialTheme.typography.button)
            }
        }
        return
    }

    if (reportState == ReportState.Default) {
        when (customerState) {
            CustomerState.ConnectionFailed -> {
            } // implemented before
            CustomerState.Default -> {
                Text(
                    text = stringResource(id = R.string.searching),
                    style = MaterialTheme.typography.subtitle2
                )
            }
            is CustomerState.Found -> CustomerInfo(customer = customerState.customer)
            CustomerState.NotFound -> {
                Text(
                    text = stringResource(id = R.string.not_found),
                    style = MaterialTheme.typography.subtitle2
                )
            }
            CustomerState.Searching -> {
                Text(
                    text = stringResource(id = R.string.searching),
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    } else {
        val phone = LocalPhoneNumberProvider.current
        when (reportState) {
            ReportState.AskToAddNewCustomer -> {
                SimpleActionButton(
                    label = stringResource(id = R.string.add_user_request, phone),
                    buttonLabel = "Register"
                ) {
                    requestNewCustomerRegistration()
                }
            }
            ReportState.ConnectionFailed -> {
            } //implemented before
            ReportState.Default -> {
            }
            is ReportState.PurposeRequested -> {
                SimpleActionButton(
                    label = stringResource(id = R.string.purpose_msg, phone),
                    buttonLabel = "Open dialog"
                ) {
                    requestPurpose()
                }
            }
            ReportState.Sending -> TODO()
            ReportState.Submitted -> TODO()
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
            Column {
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
                Text(
                    text = stringResource(id = R.string.balance, customer.balance),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.subtitle1
                )
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

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            Column(modifier = Modifier.weight(4f)) {
                Text(
                    text = stringResource(id = R.string.next_appointment),
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )
                if (customer.nextAppointment != null) {
                    val nextAppointment = customer.nextAppointment!!
                    val lastAppointmentText =
                        buildAnnotatedString {
                            withStyle(
                                MaterialTheme.typography.body1.toSpanStyle().copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(nextAppointment.diagnose + "\n")
                            }
                            withStyle(MaterialTheme.typography.caption.toSpanStyle()) {
                                append(simpleDateFormat.format(nextAppointment.date))
                                append(" - ")
                                append(nextAppointment.user)
                            }
                        }
                    Text(text = lastAppointmentText, modifier = Modifier.padding(4.dp))
                } else {
                    Text(
                        text = stringResource(R.string.no_next_appointment),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.previous_appointment),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                if (customer.lastAppointment != null) {
                    val lastAppointment = customer.lastAppointment!!
                    val lastAppointmentText =
                        buildAnnotatedString {
                            withStyle(
                                MaterialTheme.typography.body1.toSpanStyle().copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(lastAppointment.diagnose + "\n")
                            }
                            withStyle(MaterialTheme.typography.caption.toSpanStyle()) {
                                append(simpleDateFormat.format(lastAppointment.date))
                                append(" - ")
                                append(lastAppointment.user)
                            }
                        }
                    Text(text = lastAppointmentText, modifier = Modifier.padding(4.dp))
                } else {
                    Text(
                        text = stringResource(R.string.no_prev_appointment),
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleActionButton(
    label: String,
    buttonLabel: String,
    action: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Button(onClick = { action() }) {
            Text(text = buttonLabel, style = MaterialTheme.typography.button)
        }
    }
}