package uz.muhammadyusuf.kurbonov.myclinic.android.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import uz.muhammadyusuf.kurbonov.myclinic.R
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.POWER_MANAGER_INTENTS
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.allAppPermissions
import uz.muhammadyusuf.kurbonov.myclinic.android.shared.allAppPermissionsDescriptions

@ExperimentalPermissionsApi
@Composable
fun PermissionScreen() = Column(modifier = Modifier.padding(8.dp)) {
    Text(text = buildAnnotatedString {
        withStyle(MaterialTheme.typography.overline.toSpanStyle()) {
            append(stringResource(R.string.permissions_overline))
        }
        withStyle(MaterialTheme.typography.h6.toSpanStyle()) {
            append(stringResource(R.string.permissions))
        }
    })
    val context = LocalContext.current
    Spacer(modifier = Modifier.height(8.dp))
    val permissionsWithDescriptions = allAppPermissions zip
            allAppPermissionsDescriptions(LocalContext.current)
    LazyColumn {
        items(permissionsWithDescriptions) {
            val permissionState = rememberPermissionState(permission = it.first)
            PermissionItem(
                description = it.second, onRowClick = {
                    permissionState.launchPermissionRequest()
                },
                hasPermission = permissionState.hasPermission,
                shouldShowRationale = permissionState.shouldShowRationale
            )
        }
        item {
            var update by remember {
                mutableStateOf(0)
            }
            val overlayActivityResult =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    update++
                }

            val grantedState by produceState(
                initialValue = false,
                update
            ) {
                value = Settings.canDrawOverlays(context)
            }
            PermissionItem(
                description = "Overlay drawing",
                onRowClick = {
                    overlayActivityResult.launch(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                },
                hasPermission = grantedState,
                shouldShowRationale = !grantedState
            )

        }


        val powerMgmtIntent = POWER_MANAGER_INTENTS.find {
            context.packageManager.resolveActivity(
                it, MATCH_DEFAULT_ONLY
            ) != null
        }

        if (powerMgmtIntent != null) {

            item {

                var update by remember {
                    mutableStateOf(0)
                }

                var accessDenied by remember {
                    mutableStateOf(false)
                }


                val powerMgmtResult =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {
                        update++
                    }

                val grantedState by produceState(
                    initialValue = true,
                    update
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        value =
                            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                                .isBackgroundRestricted
                    }
                }

                if (!accessDenied) {
                    Dialog(onDismissRequest = { accessDenied = false }) {
                        Text(text = stringResource(id = R.string.no_power_mangement_access))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White, Color.Yellow)
                            )
                        )
                        .clickable {
                            try {
                                powerMgmtResult.launch(powerMgmtIntent)
                            } catch (e: Exception) {
                                accessDenied = true
                            }
                        }, verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = stringResource(R.string.power_management_options_description),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )



                    when {
                        grantedState -> {
                            Icon(
                                modifier = Modifier.padding(2.dp),
                                imageVector = Icons.Default.Done,
                                contentDescription = "", tint = Color.White
                            )
                        }
                        !grantedState -> {
                            Icon(
                                modifier = Modifier.padding(2.dp),
                                imageVector = Icons.Default.Warning,
                                contentDescription = "", tint = Color.Yellow
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@Composable
fun PermissionItem(
    modifier: Modifier = Modifier,
    description: String,
    rationale: String = "",
    onRowClick: () -> Unit,
    hasPermission: Boolean,
    shouldShowRationale: Boolean
) = Card(
    modifier = modifier
        .fillMaxWidth()
        .padding(4.dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, if (hasPermission) Color.Green else Color.Red)
                )
            )
            .clickable {
                onRowClick()
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.subtitle1
            )
            if (shouldShowRationale) {
                Text(
                    text = rationale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }
        }
        when {
            hasPermission -> {
                Icon(
                    modifier = Modifier.padding(2.dp),
                    imageVector = Icons.Default.Done,
                    contentDescription = "", tint = Color.White
                )
            }
            !hasPermission -> {
                Icon(
                    modifier = Modifier.padding(2.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = "", tint = Color.Yellow
                )
            }
        }
    }
}

@ExperimentalPermissionsApi
@Preview(showBackground = true)
@Composable
fun GrantedPermissionItem() {
    PermissionItem(
        modifier = Modifier.fillMaxWidth(),
        description = "Hallo",
        onRowClick = {
        },
        hasPermission = true,
        shouldShowRationale = false
    )
}

@ExperimentalPermissionsApi
@Preview(showBackground = true)
@Composable
fun NotGrantedPermissionItem() {
    PermissionItem(
        modifier = Modifier.fillMaxWidth(),
        description = "Hallo",
        onRowClick = {
        },
        hasPermission = false,
        shouldShowRationale = false
    )
}

@ExperimentalPermissionsApi
@Preview(showBackground = true)
@Composable
fun NotGrantedPermissionItemWithDescription() {
    PermissionItem(
        modifier = Modifier.fillMaxWidth(),
        description = "Hallo",
        rationale = "This permission is required for text messages being read. Please give me that",
        onRowClick = {
        },
        hasPermission = false,
        shouldShowRationale = true
    )
}