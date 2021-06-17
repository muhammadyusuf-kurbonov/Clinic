package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.filters.LargeTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import uz.muhammadyusuf.kurbonov.myclinic.core.tests.SearchTests
import uz.muhammadyusuf.kurbonov.myclinic.network.tests.uz.muhammadyusuf.kurbonov.myclinic.network.tests.NetworkTests

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SearchTests::class,
    NetworkTests::class
)
@LargeTest
class GeneralTests