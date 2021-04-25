package uz.muhammadyusuf.kurbonov.myclinic

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    StatesTests::class,
    NotificationViewTests::class,
    IntegratedTests::class
)
class GeneralTests