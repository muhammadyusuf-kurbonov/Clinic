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
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection

@RunWith(AndroidJUnit4::class)
class IntegratedTests {
    private val mockWebServer = MockWebServer()

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val uiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).apply {
            pressHome()
        }
    }

    @Before
    fun initialize() {

        Log.d("test-system", "Initializing...")

        createNotificationChannel(context)

        mockWebServer.start()

        val api = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(mockWebServer.url("/"))
            .client(DI.getOkHTTPClient())
            .build()
            .create(APIService::class.java)
        App.appViewModel = AppViewModel(api)

        App.getAppViewModelInstance().reduce(Action.Start(context))
        Log.d("test-system", "Initialized")
    }

    @After
    fun dismiss() {
        App.getAppViewModelInstance().reduce(Action.Finish)
        mockWebServer.shutdown()
        println("End test")
    }


    @Test
    fun testNotFound() {
        runBlocking {
            mockWebServer.enqueueResponse("not-found.json", 404)
            App.getAppViewModelInstance()
                .reduce(Action.Search("+998945886633", CallDirection.INCOME))



            checkNotificationWithText(uiDevice, context.getString(R.string.not_found))
        }
    }

    @Test
    fun testFound() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 200)
            App.getAppViewModelInstance()
                .reduce(Action.Search("+998994801416", CallDirection.OUTGOING))
            uiDevice.wait(Until.findObject(By.textContains("Иван")), 15000)
        }
    }

    @Test
    fun testAuthRequest() {
        runBlocking {
            mockWebServer.enqueueResponse("found.json", 401)
            App.getAppViewModelInstance()
                .reduce(Action.Search("+998994801416", CallDirection.OUTGOING))


            checkNotificationWithText(uiDevice, context.getString(R.string.auth_request))
        }
    }

    @Test
    fun testNoResponse() {
        runBlocking {
            App.getAppViewModelInstance()
                .reduce(Action.Search("+998994801416", CallDirection.OUTGOING))

            mockWebServer.shutdown()

            checkNotificationWithText(uiDevice, context.getString(R.string.no_connection))
        }
    }

}