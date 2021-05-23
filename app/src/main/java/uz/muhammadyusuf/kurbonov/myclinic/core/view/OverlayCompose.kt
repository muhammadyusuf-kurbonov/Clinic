package uz.muhammadyusuf.kurbonov.myclinic.core.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.core.State

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OverlayCompose(state: State = State.Started) {

    var expanded by remember {
        mutableStateOf(true)
    }

    Row(verticalAlignment = Alignment.Bottom) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "overlayViewImage",
            modifier = Modifier
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


        AnimatedVisibility(visible = expanded) {

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomEnd = 8.dp,
                            bottomStart = 0.dp
                        )
                    )
                    .padding(4.dp)
            ) {
                Content(state)
            }
        }
    }
}

@Composable
private fun Content(state: State) {
    Text(
        text = state.javaClass.simpleName
    )
}

@Preview()
@Composable
fun PreviewOverlayCompose() {
    OverlayCompose(State.Searching)
}