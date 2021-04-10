package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.muhammadyusuf.kurbonov.myclinic.di.DI
import uz.muhammadyusuf.kurbonov.myclinic.network.APIService
import uz.muhammadyusuf.kurbonov.myclinic.network.authentification.AuthRequest
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.Action
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.AppViewModel
import uz.muhammadyusuf.kurbonov.myclinic.viewmodels.State
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AppViewModelTest {


    companion object {
        private var oldUrl = ""
    }

    private fun changeBaseUrl(url: String) {
        if (oldUrl == url)
            return
        println("URL changed to $url")
        oldUrl = url
        App.appViewModel.reduceBlocking(Action.Finish)
        val api = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .client(DI.getOkHTTPClient())
            .build()
            .create(APIService::class.java)
        val viewModel = AppViewModel(api)
        App.appViewModel = viewModel
        App.appViewModel.reduceBlocking(Action.Start(InstrumentationRegistry.getInstrumentation().context))
    }

    private val mockWebServer = MockWebServer()

    @Before
    fun prepare() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            mockWebServer.start(8080)
            changeBaseUrl(mockWebServer.url("/").toString())

            val authService by lazy {
                DI.getAPIService()
            }
            App.appViewModel.reduce(Action.Start(context))
            val response = authService.authenticate(
                AuthRequest(
                    email = "demo@32desk.com",
                    password = "demo"
                )
            )

            if (response.isSuccessful) {
                App.pref.edit()
                    .putString("token", response.body()?.accessToken)
                    .apply()
            }
        }
    }

    @Test
    fun testInitialization() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduceBlocking(Action.Start(context))
            assert(true)
        }
    }

    @Test
    fun testFoundCustomer() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            mockWebServer.enqueueResponse("found.json", 200)

            App.appViewModel.reduce(Action.Search("+998994801416"))

            assertTrue(
                "State is ${App.appViewModel.state.value}",
                App.appViewModel.state.value is State.Found
            )
        }
    }

    @Test
    fun testCustomerNotFound() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            mockWebServer.enqueueResponse("not-found.json", 404)

            App.appViewModel.reduce(Action.Search("+99894801416"))
            assertEquals(State.NotFound, App.appViewModel.state.value)
        }
    }

    @Test
    fun testAddNewUser() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            mockWebServer.enqueueResponse("not-found.json", 404)

            App.appViewModel.reduce(Action.Search("+998945886633"))
            assertTrue(App.appViewModel.state.value is State.NotFound)

            mockWebServer.enqueueResponse("authentification_success.json", 200)
            App.appViewModel.reduce(Action.EndCall(context, "+998945886633"))
            assertTrue(App.appViewModel.state.value is State.AddNewCustomerRequest)
        }
    }

    @Test
    fun testNoConnection() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().context
            App.appViewModel.reduce(Action.Start(context))

            App.appViewModel.reduce(Action.SetNoConnectionState)
            assertEquals(State.ConnectionError, App.appViewModel.state.value)
        }
    }


    @After
    fun tearDown() {
        mockWebServer.shutdown()
        App.appViewModel.reduceBlocking(Action.Finish)
        println("End test")
    }

    private fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
        val inputStream = javaClass.classLoader?.getResourceAsStream("api-response/$fileName")

        val source = inputStream?.let { inputStream.buffered() }
        source?.let {
            enqueue(
                MockResponse()
                    .setResponseCode(code)
                    .setBody(source.bufferedReader(StandardCharsets.UTF_8).readText())
            )
        }
    }
}