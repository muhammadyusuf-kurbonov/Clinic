package uz.muhammadyusuf.kurbonov.myclinic

import org.junit.Assert
import org.junit.Test
import uz.muhammadyusuf.kurbonov.myclinic.utils.maskPhoneNumber


class PhoneMaskTest {
    @Test
    fun testMask() {
        Assert.assertEquals("+9989139***38", "+998913975538".maskPhoneNumber())
    }
}