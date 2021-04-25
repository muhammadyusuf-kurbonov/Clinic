package uz.muhammadyusuf.kurbonov.myclinic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uz.muhammadyusuf.kurbonov.myclinic.core.NotificationView
import uz.muhammadyusuf.kurbonov.myclinic.core.State

@RunWith(AndroidJUnit4::class)
class NotificationViewTests {

    private lateinit var notificationView: NotificationView
    private val state = MutableStateFlow<State>(State.None)

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Before
    fun prepare() {
        state.value = State.None

        createNotificationChannel(context)


        notificationView = NotificationView(context, state)
        notificationView.start()
    }

    @Test
    fun testNotFound() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.NotFound
        uiDevice.openNotification()

        uiDevice.wait(Until.findObject(By.text(context.getString(R.string.not_found))), 15000)

        val text = uiDevice.findObject(By.text(context.getString(R.string.not_found)))
        assertEquals(context.getString(R.string.not_found), text.text)
    }

    @Test
    fun testAuthRequest() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.AuthRequest("+998913975538")
        uiDevice.openNotification()

        uiDevice.wait(Until.findObject(By.text(context.getString(R.string.auth_text))), 15000)

        val text = uiDevice.findObject(By.text(context.getString(R.string.auth_text)))
        assertEquals(context.getString(R.string.auth_text), text.text)
    }

    @Test
    fun testAddNewCustomer() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.AddNewCustomerRequest("+998913975538")
        uiDevice.openNotification()

        uiDevice.wait(
            Until.findObject(
                By.text(
                    context.getString(
                        R.string.add_user_request,
                        "+998913975538"
                    )
                )
            ), 15000
        )

        val text = uiDevice.findObject(
            By.text(
                context.getString(
                    R.string.add_user_request,
                    "+998913975538"
                )
            )
        )
        assertEquals(
            context.getString(
                R.string.add_user_request,
                "+998913975538"
            ), text.text
        )
    }


    @Test
    fun testConnectionError() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.ConnectionError
        uiDevice.openNotification()

        uiDevice.wait(Until.findObject(By.text(context.getString(R.string.no_connection))), 15000)

        val text = uiDevice.findObject(By.text(context.getString(R.string.no_connection)))
        assertEquals(context.getString(R.string.no_connection), text.text)
    }


    @Test
    fun testTooSlowConnection() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.TooSlowConnectionError
        uiDevice.openNotification()

        checkNotificationWithText(uiDevice, context.getString(R.string.too_slow))
    }

    @Test
    fun testSearching() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        state.value = State.Searching
        uiDevice.openNotification()

        checkNotificationWithText(uiDevice, context.getString(R.string.searching_text))
    }

}