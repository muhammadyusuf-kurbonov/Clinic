package uz.muhammadyusuf.kurbonov.myclinic.android.views.overlay

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
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


@Composable
fun OverlayTheme(
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
