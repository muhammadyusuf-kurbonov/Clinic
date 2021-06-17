package uz.muhammadyusuf.kurbonov.myclinic.android.views.overlay

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.LoginActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NewCustomerActivity
import uz.muhammadyusuf.kurbonov.myclinic.android.activities.NoteActivity
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.core.models.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.core.models.Customer

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OverlayCompose(
    state: State = State.Started,
    onFinished: () -> Unit = {}
) {

    var expanded by remember {
        mutableStateOf(true)
    }

    val offset = animateDpAsState(targetValue = if (expanded) 0.dp else (-16).dp)

    OverlayTheme {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "overlayViewImage",
                modifier = Modifier
                    .offset(offset.value, 0.dp)
                    .width(48.dp)
                    .height(48.dp)
                    .shadow(10.dp, shape = CircleShape)
                    .background(Color.White, shape = CircleShape)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        expanded = !expanded
                    }
                    .padding(8.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedVisibility(visible = expanded) {
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 8.dp,
                                bottomEnd = 8.dp,
                                bottomStart = 8.dp
                            )
                        )
                        .padding(4.dp)
                ) {
                    Content(Modifier.padding(4.dp), state, fadeToVisibility = {
                        expanded = it
                    }) {
                        scope.launch {
                            delay(2000)
                            onFinished()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: State,
    fadeToVisibility: (Boolean) -> Unit = {},
    finish: () -> Unit = {}
) = Box(modifier = modifier) {
    when (state) {
        is State.Started -> {
            Text(text = stringResource(id = R.string.searching_text))
        }
        is State.AddNewCustomerRequest -> {
            Column {
                val context = LocalContext.current
                Text(
                    text = stringResource(id = R.string.add_user_request, state.phone),
                    modifier = Modifier.clickable {
                        fadeToVisibility(false)
                        context.startActivity(
                            Intent(
                                context,
                                NewCustomerActivity::class.java
                            ).apply {
                                putExtra("phone", state.phone)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        finish()
                    }, textAlign = TextAlign.Center
                )
            }
        }
        is State.AuthRequest -> {
            Column {
                Text(text = stringResource(id = R.string.auth_request))
                val context = LocalContext.current
                context.startActivity(Intent(context, LoginActivity::class.java).apply {
                    putExtra(LoginActivity.EXTRA_PHONE, state.phone)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
        is State.Error -> {
            Text(text = stringResource(id = R.string.unknown_error))
        }
        State.Finished -> {
        }
        is State.Found -> {
            CustomerInfo(customer = state.customer, callDirection = state.callDirection)
        }
        State.NoConnectionState -> {
            Text(text = stringResource(id = R.string.no_connection))
        }
        State.None -> {
            fadeToVisibility(false)
        }
        State.NotFound -> {
            Text(text = stringResource(id = R.string.not_found))
        }
        is State.PurposeRequest -> {
            Text(text = stringResource(id = R.string.purpose_msg, state.customer.name))
            val context = LocalContext.current
            context.startActivity(Intent(context, NoteActivity::class.java).apply {
                putExtra(
                    "communicationId", state.communicationId
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
        State.Searching -> {
            Text(text = stringResource(id = R.string.searching_text))
        }
        State.ConnectionTimeoutState -> {
            Text(text = stringResource(id = R.string.read_timeout))
        }
    }
}

@Composable
fun CustomerInfo(customer: Customer, callDirection: CallDirection) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .height(32.dp)
                    .width(32.dp)
                    .padding(4.dp),
                painter = painterResource(
                    id = when (callDirection) {
                        CallDirection.INCOME ->
                            R.drawable.ic_phone_in_24
                        CallDirection.OUTGOING ->
                            R.drawable.ic_phone_outgoing
                    }
                ), contentDescription = "Call direction icon",
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
            val nameAndPhone = buildAnnotatedString {

                withStyle(MaterialTheme.typography.h6.toSpanStyle()) {
                    append(customer.name + "\n")
                }

                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colors.onBackground
                            .copy(alpha = ContentAlpha.disabled)
                    )
                ) {
                    append(customer.phoneNumber)
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
            Column(modifier = Modifier.weight(4f)) {
                Text(
                    text = stringResource(id = R.string.next_appointment),
                    fontWeight = FontWeight.Bold
                )
                if (customer.nextAppointment != null) {
                    val nextAppointment = customer.nextAppointment!!
                    val lastAppointmentText =
                        "${nextAppointment.date} - ${nextAppointment.doctor?.name ?: ""} - ${nextAppointment.diagnosys}"
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
                        "${lastAppointment.date} - ${lastAppointment.doctor?.name ?: ""} - ${lastAppointment.diagnosys}"
                    Text(text = lastAppointmentText, modifier = Modifier.padding(4.dp))
                } else {
                    Text(
                        text = "No previous appointment yet", modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            val painter = rememberCoilPainter(
                request = customer.avatarLink,
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
