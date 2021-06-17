package uz.muhammadyusuf.kurbonov.myclinic.android.activities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import uz.muhammadyusuf.kurbonov.myclinic.android.views.overlay.*

private val DarkColorPalette = darkColors(
    primary = PRIMARY_DARK_COLOR,
    primaryVariant = PRIMARY_VARIANT_DARK_COLOR,
    secondary = SECONDARY_DARK_COLOR
)

private val LightColorPalette = lightColors(
    primary = PRIMARY_LIGHT_COLOR,
    primaryVariant = PRIMARY_VARIANT_LIGHT_COLOR,
    secondary = SECONDARY_LIGHT_COLOR

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun MyClinicTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}