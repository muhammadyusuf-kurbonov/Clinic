package uz.muhammadyusuf.kurbonov.myclinic.core.models

sealed class LifecycleEvents {
    object Started : LifecycleEvents()
    object Initialized : LifecycleEvents()
    object Finished : LifecycleEvents()
}
