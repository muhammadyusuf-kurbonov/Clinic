package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.filters.LargeTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    IntegratedTests::class
)
@LargeTest
class GeneralTests