package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

val LocalNavigation = staticCompositionLocalOf<NavController> {
    error("No default implementation")
}