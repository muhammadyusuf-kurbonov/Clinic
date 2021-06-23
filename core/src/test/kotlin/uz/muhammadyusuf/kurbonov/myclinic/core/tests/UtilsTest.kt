package uz.muhammadyusuf.kurbonov.myclinic.core.tests

import org.junit.Test
import uz.muhammadyusuf.kurbonov.myclinic.core.CallDirection
import uz.muhammadyusuf.kurbonov.myclinic.network.models.CommunicationType

class UtilsTest {
    @Test
    fun `call direction and communicationType are same`() {
        @Suppress("USELESS_IS_CHECK")
        assert(CommunicationType.INCOMING is CallDirection)
    }
}