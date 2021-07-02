package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
    if (Build.MANUFACTURER.contains("huawei", true))
        add(context.getString(R.string.huawei_use_component_desc))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        add(context.getString(R.string.foreground_permission_desc))
}.toTypedArray()

val POWER_MANAGER_INTENTS = arrayOf(
    //region HAWEI
    Intent().setComponent(
        ComponentName(
            "com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity"
        )
    ),
    Intent().setComponent(
        ComponentName(
            "com.huawei.systemmanager",
            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
        )
    ),
    Intent().setComponent(
        ComponentName(
            "com.huawei.systemmanager",
            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
        )
    ),
    //endregion

    //region XIAOMI
    Intent().setComponent(
        ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
    ),
    Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT),
    Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
    Intent().setComponent(
        ComponentName(
            "com.miui.securitycenter",
            "com.miui.powercenter.PowerSettings"
        )
    ),
    //endregion

    //region LETV
    Intent().setComponent(
        ComponentName(
            "com.letv.android.letvsafe",
            "com.letv.android.letvsafe.AutobootManageActivity"
        )
    ),
    //endregion


    //region COLOROS
    Intent().setComponent(
        ComponentName(
            "com.coloros.safecenter",
            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
        )
    ),
    Intent().setComponent(
        ComponentName(
            "com.coloros.safecenter",
            "com.coloros.safecenter.startupapp.StartupAppListActivity"
        )
    ),
    //endregion


    //region OPPO
    Intent().setComponent(
        ComponentName(
            "com.oppo.safe",
            "com.oppo.safe.permission.startup.StartupAppListActivity"
        )
    ),
    //endregion


    //region IQOO
    Intent().setComponent(
        ComponentName(
            "com.iqoo.secure",
            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
        )
    ),
    Intent().setComponent(
        ComponentName(
            "com.iqoo.secure",
            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
        )
    ),
    //endregion


    //region VIVO
    Intent().setComponent(
        ComponentName(
            "com.vivo.permissionmanager",
            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
        )
    ),
    //endregion


    //region SAMSUNG
    Intent().setComponent(
        ComponentName(
            "com.samsung.android.lool",
            "com.samsung.android.sm.ui.battery.BatteryActivity"
        )
    ),
    //endregion

    //region HTC
    Intent().setComponent(
        ComponentName(
            "com.htc.pitroad",
            "com.htc.pitroad.landingpage.activity.LandingPageActivity"
        )
    ),
    //endregion


    //region ASUS
    Intent().setComponent(
        ComponentName(
            "com.asus.mobilemanager",
            "com.asus.mobilemanager.MainActivity"
        )
    ),
    //endregion

)