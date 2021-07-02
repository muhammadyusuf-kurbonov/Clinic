package uz.muhammadyusuf.kurbonov.myclinic.android.shared

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import uz.muhammadyusuf.kurbonov.myclinic.core.AppStatesController

val LocalNavigation = staticCompositionLocalOf<NavController> {
    error("No default implementation")
}

val LocalAppControllerProvider = staticCompositionLocalOf<AppStatesController> {
    error("Not initialized yet")
}

val LocalPhoneNumberProvider = compositionLocalOf<String> {
    error("Not implemented")
}