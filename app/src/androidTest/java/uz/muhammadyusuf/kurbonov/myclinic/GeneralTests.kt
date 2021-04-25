package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.filters.LargeTest
import org.junit.AfterClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import uz.muhammadyusuf.kurbonov.myclinic.core.Action

@RunWith(Suite::class)
@Suite.SuiteClasses(
    StatesTests::class,
    NotificationViewTests::class,
    IntegratedTests::class
)
@LargeTest
class GeneralTests {
    companion object {
        @AfterClass
        fun close() {
            App.appViewModel.reduceBlocking(Action.Finish)
        }
    }
}