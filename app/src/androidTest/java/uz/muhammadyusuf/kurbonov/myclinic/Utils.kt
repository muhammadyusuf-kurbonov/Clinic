package uz.muhammadyusuf.kurbonov.myclinic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import junit.framework.TestCase


internal fun findText(uiDevice: UiDevice, text: String) {
    uiDevice.wait(Until.findObject(By.text(text)), 15000)

    val notification = uiDevice.findObject(By.text(text))
    TestCase.assertEquals(text, notification.text)
}

fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        App.NOTIFICATION_CHANNEL_ID,
        "Notifications of app 32Desk.com",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    channel.enableVibration(true)
    NotificationManagerCompat.from(context)
        .createNotificationChannel(channel)
}