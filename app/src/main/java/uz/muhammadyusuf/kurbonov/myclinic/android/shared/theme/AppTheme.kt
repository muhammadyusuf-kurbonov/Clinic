package uz.muhammadyusuf.kurbonov.myclinic.android.shared.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val PRIMARY_LIGHT_COLOR = Color(0xFF30C0FF)
val PRIMARY_VARIANT_LIGHT_COLOR = Color(0xFF196D91)
val SECONDARY_LIGHT_COLOR = Color(0xFF30a1d2)
val PRIMARY_DARK_COLOR = Color(0xFF006491)
val PRIMARY_VARIANT_DARK_COLOR = Color(0xFF1D5D7A)
val SECONDARY_DARK_COLOR = Color(0xFF1E81AC)

val ERROR_LIGHT_COLOR = Color(0xFFE73E3E)
val ERROR_DARK_COLOR = Color(0xFF8F0000)

val PERMISSION_GRANTED_LIGHT_COLOR = Color(0xFF5DE263)
val PERMISSION_GRANTED_DARK_COLOR = Color(0xFF006405)

val PERMISSION_NOT_GRANTED_LIGHT_COLOR = ERROR_LIGHT_COLOR
val PERMISSION_NOT_GRANTED_DARK_COLOR = ERROR_DARK_COLOR

val PERMISSION_NEUTRAL_LIGHT_COLOR = Color(0xFFFFEB3B)
val PERMISSION_NEUTRAL_DARK_COLOR = Color(0xFFC0940F)

val Colors.permissionGranted: Color
    @Composable
    get() = if (isSystemInDarkTheme()) PERMISSION_GRANTED_DARK_COLOR
    else PERMISSION_GRANTED_LIGHT_COLOR

val Colors.permissionNotGranted: Color
    @Composable
    get() = if (isSystemInDarkTheme()) PERMISSION_NOT_GRANTED_DARK_COLOR
    else PERMISSION_NOT_GRANTED_LIGHT_COLOR

val Colors.permissionNeutral: Color
    @Composable
    get() = if (isSystemInDarkTheme()) PERMISSION_NEUTRAL_DARK_COLOR
    else PERMISSION_NEUTRAL_LIGHT_COLOR

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val lightColors = lightColors(
        primary = PRIMARY_LIGHT_COLOR,
        primaryVariant = PRIMARY_VARIANT_LIGHT_COLOR,
        secondary = SECONDARY_LIGHT_COLOR,
        error = ERROR_LIGHT_COLOR
    )
    val darkColors = darkColors(
        primary = PRIMARY_DARK_COLOR,
        primaryVariant = PRIMARY_VARIANT_DARK_COLOR,
        secondary = SECONDARY_DARK_COLOR,
        error = ERROR_DARK_COLOR
    )
    MaterialTheme(
        if (isSystemInDarkTheme()) darkColors else lightColors
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onSurface) {
            content()
        }
    }
}
