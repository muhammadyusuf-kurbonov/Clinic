package uz.muhammadyusuf.kurbonov.myclinic

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import uz.muhammadyusuf.kurbonov.myclinic.core.State
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.utils.CallDirection

@RunWith(
    AndroidJUnit4::class
)
class StatesTests {

    private val mockWebServer = MockWebServer()
    private lateinit var viewModel: AppViewModel

    @Before
    fun initialize() {

        Log.d("test-system", "Initializing...")

        mockWebServer.start()

        createNotificationChannel(InstrumentationRegistry.getInstrumentation().targetContext)

        val api = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(mockWebServer.url("/"))
            .client(DI.getOkHTTPClient())
            .build()
            .create(APIService::class.java)
        viewModel = AppViewModel(api)
        viewModel.reduceBlocking(Action.Start(InstrumentationRegistry.getInstrumentation().context))

        Log.d("test-system", "Initialized")
    }

    @After
    fun dismiss() {
        viewModel.reduceBlocking(Action.Finish)
        mockWebServer.shutdown()
        println("End test")
    }

    @Test
    fun testFoundCustomer() {
        runBlocking {

            mockWebServer.enqueueResponse("found.json", 200)
            viewModel.reduce(Action.Search("+998994801416", CallDirection.INCOME))

            viewModel.state.waitUntil(15000) {
                it is State.Found
            }
        }
    }

    @Test
    fun testAuthRequest() {
        runBlocking {

            mockWebServer.enqueueResponse("not-found.json", 401)
            viewModel.reduce(Action.Search("+998945886633", CallDirection.OUTGOING))
            viewModel.state.waitUntil(15000) { it is State.AuthRequest }
        }
    }

    @Test
    fun testNotFound() {
        runBlocking {

            mockWebServer.enqueueResponse("not-found.json", 404)
            viewModel.reduce(Action.Search("+998945886633", CallDirection.INCOME))
            viewModel.state.waitUntil(15000) { it is State.NotFound }
        }
    }

    @Test
    fun testNoConnection() {
        runBlocking {
            viewModel.reduce(Action.SetNoConnectionState)
            viewModel.state.waitUntil(15000) {
                it == State.NoConnectionState
            }
        }
    }
}