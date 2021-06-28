package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel

val LocalNavigation = staticCompositionLocalOf<NavController> {
    error("No default implementation")
}

val AppViewModelProvider = staticCompositionLocalOf<AppViewModel> {
    error("Not initialized yet")
}