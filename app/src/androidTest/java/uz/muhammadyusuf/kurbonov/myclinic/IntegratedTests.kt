package uz.muhammadyusuf.kurbonov.myclinic

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.muhammadyusuf.kurbonov.myclinic.api.APIService
import uz.muhammadyusuf.kurbonov.myclinic.core.Action
import uz.muhammadyusuf.kurbonov.myclinic.core.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.core.NotificationView
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection

@RunWith(AndroidJUnit4::class)
class IntegratedTests {
    private lateinit var notificationView: NotificationView

    private val mockWebServer = MockWebServer()
    private lateinit var viewModel: AppViewModel

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val uiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Before
    fun initialize() {

        Log.d("test-system", "Initializing...")

        mockWebServer.start()

        val api = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(mockWebServer.url("/"))
            .client(DI.getOkHTTPClient())
            .build()
            .create(APIService::class.java)
        viewModel = AppViewModel(api)

        viewModel.reduceBlocking(Action.Start(context))
        notificationView = NotificationView(context, viewModel.state)
        notificationView.start()

        Log.d("test-system", "Initialized")
    }

    @After
    fun dismiss() {
        mockWebServer.shutdown()
        println("End test")
    }


    @Test
    fun testNotFound() {
        runBlocking {
            mockWebServer.enqueueResponse("not-found.json", 404)
            viewModel.reduce(Action.Search("+998945886633", CallDirection.INCOME))

            uiDevice.openNotification()

            checkNotificationWithText(uiDevice, context.getString(R.string.not_found))
        }
    }

    @Test
    fun testFound() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 200)
            viewModel.reduce(Action.Search("+998994801416", CallDirection.OUTGOING))

            uiDevice.openNotification()
            uiDevice.wait(Until.findObject(By.textContains("Иван")), 15000)
        }
    }
}