package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginTests::class,
    SearchTests::class,
    CustomerDTOMapperTest::class,
    UtilsTest::class
)
class CoreTests