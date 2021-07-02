package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uz.muhammadyusuf.kurbonov.myclinic.BuildConfig
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.LocalNavigation

@Composable
fun MainScreen(permissionsGranted: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        val navController = LocalNavigation.current
        Column(modifier = Modifier.align(Alignment.Center)) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.h4
            )

            //TODO: Remove, it's for test
            Button(onClick = { navController.navigate("service_test") }) {
                Text(text = "Service test")
            }
        }

        Text(
            text = stringResource(
                id = R.string.version_template,
                stringResource(id = R.string.app_name),
                BuildConfig.VERSION_NAME
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(4.dp)
        )

        if (!permissionsGranted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Card(modifier = Modifier.clickable {
                    navController.navigate("permissions")
                }) {
                    Surface(color = Color.Red, contentColor = Color.White) {
                        Text(
                            text = stringResource(id = R.string.ask_permissions),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MainScreenWithGrantedPermissions() {
    MainScreen(permissionsGranted = true)
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MainScreenWithoutGrantedPermissions() {
    MainScreen(permissionsGranted = false)
}