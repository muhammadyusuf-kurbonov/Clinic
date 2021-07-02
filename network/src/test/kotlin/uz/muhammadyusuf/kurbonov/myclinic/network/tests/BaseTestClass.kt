package uz.muhammadyusuf.kurbonov.myclinic.network.tests

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import uz.muhammadyusuf.kurbonov.myclinic.network.AppRepository

open class BaseTestClass {
    protected val mockWebServer = MockWebServer()
    protected lateinit var appRepository: AppRepository

    @Before
    fun initialize() {
        mockWebServer.start()
        appRepository = AppRepository("dummy", mockWebServer.url("/").toString())
    }

    @After
    fun dismiss() {
        mockWebServer.shutdown()
    }

}