package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AuthTests::class,
    SearchTests::class,
    CommunicationsTests::class,
    UpdateCommunicationTests::class,
    AddCustomerTests::class
)
class NetworkTests