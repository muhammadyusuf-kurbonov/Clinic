package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import android.Manifest
import android.content.Context
import android.os.Build
import uz.muhammadyusuf.kurbonov.myclinic.R

val allAppPermissions = mutableListOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_CALL_LOG,
).apply {
    if (Build.MANUFACTURER.contains("huawei", false))
        add("com.huawei.permission.external_app_settings.USE_COMPONENT")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        add(Manifest.permission.FOREGROUND_SERVICE)
}.toTypedArray()

// TODO: Add rationales too

fun allAppPermissionsDescriptions(context: Context) = mutableListOf(
    context.getString(R.string.read_call_state_desc),
    context.getString(R.string.read_call_log_desc),
).apply {
    if (Build.MANUFACTURER.contains("huawei", false))
        add(context.getString(R.string.huawei_use_component_desc))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        add(context.getString(R.string.foreground_permission_desc))
}.toTypedArray()