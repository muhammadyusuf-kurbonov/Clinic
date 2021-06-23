package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import android.Manifest
import android.os.Build

val allAppPermissions = mutableListOf(
    Manifest.permission.READ_PHONE_STATE,
    Manifest.permission.READ_CALL_LOG,
    "android.permission.READ_PRIVILEGED_PHONE_STATE",
).apply {
    if (Build.MANUFACTURER.contains("huawei", false))
        add("com.huawei.permission.external_app_settings.USE_COMPONENT")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        add(Manifest.permission.FOREGROUND_SERVICE)
}.toTypedArray()