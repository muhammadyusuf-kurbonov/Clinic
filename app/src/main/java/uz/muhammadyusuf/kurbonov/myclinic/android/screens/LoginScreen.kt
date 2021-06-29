package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.imePadding
import kotlinx.coroutines.delay
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalAppControllerProvider
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStateStore
import uz.muhammadyusuf.kurbonov.myclinic.core.states.AuthState

@Composable
fun LoginScreen() {
    val appViewModel = LocalAppControllerProvider.current
    val state = AppStateStore.authState.collectAsState()

    LoginForm(state) { email, password ->
        appViewModel.handle(Action.Login(email, password))
    }
}

@Composable
fun LoginForm(
    loginState: State<AuthState>,
    login: (String, String) -> Unit,
) {
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = ""
            )
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))

            EmailField(
                state = email,
                onValueChange = {
                    email = it
                },
                loginState
            )

            PasswordField(
                state = password,
                onValueChange = {
                    password = it
                }, loginState
            )

            Button(onClick = {
                login(
                    email,
                    password
                )
            }, enabled = loginState.value !is AuthState.Authenticating) {
                Text(
                    text = stringResource(id = R.string.login),
                    style = MaterialTheme.typography.button
                )
            }

            when (loginState.value) {
                is AuthState.AuthSuccess -> {
                    Text(text = stringResource(id = R.string.login_success), color = Color.Green)
                    val navController = LocalNavigation.current
                    LaunchedEffect(key1 = "navigate") {
                        delay(2000)
                        navController.navigate("main")
                    }
                }
                is AuthState.AuthFailed -> {
                    Text(text = stringResource(id = R.string.login_failed), color = Color.Red)
                }
                is AuthState.Authenticating -> {
                    Text(text = stringResource(id = R.string.authenticating))
                }
                AuthState.ConnectionFailed -> {
                    Text(text = stringResource(id = R.string.no_internet_connection))
                }
                else -> {
                }
            }
        }
    }
}

@Composable
fun EmailField(
    state: String,
    onValueChange: (String) -> Unit,
    loginState: State<AuthState>,
) {
    OutlinedTextField(
        value = state, onValueChange = onValueChange,
        modifier = Modifier
            .padding(4.dp),
        label = {
            Text(text = stringResource(id = R.string.email_hint))
        },
        isError = loginState.value == AuthState.ValidationFailed,
        singleLine = true
    )
}

@Composable
fun PasswordField(
    state: String,
    onValueChange: (String) -> Unit,
    loginState: State<AuthState>,
) {
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = state, onValueChange = onValueChange,
        label = {
            Text(text = stringResource(id = R.string.password_hint))
        },
        modifier = Modifier
            .padding(4.dp),
        isError = loginState.value == AuthState.ValidationFailed,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                    painter = painterResource(
                        id = if (isPasswordVisible)
                            R.drawable.ic_baseline_visibility_off_24 else
                            R.drawable.ic_baseline_visibility_24
                    ), contentDescription = ""
                )
            }
        },
        visualTransformation = if (isPasswordVisible)
            VisualTransformation.None else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        })
    )

}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginFormPreview() {
    LoginForm(
        produceState(
            initialValue = AuthState.Authenticating,
            producer = {})
    ) { email, password ->
        println("Logged in with $email and $password")
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginFailedFormPreview() {
    LoginForm(
        produceState(
            initialValue = AuthState.AuthFailed,
            producer = {})
    ) { email, password ->
        println("Logged in with $email and $password")
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginSuccessFormPreview() {
    LoginForm(
        produceState(
            initialValue = AuthState.AuthSuccess,
            producer = {})
    ) { email, password ->
        println("Logged in with $email and $password")
    }
}