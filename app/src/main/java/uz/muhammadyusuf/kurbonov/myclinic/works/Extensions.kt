package uz.muhammadyusuf.kurbonov.myclinic.works

import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import uz.muhammadyusuf.kurbonov.myclinic.R

fun getBaseNotification(context: Context): NotificationCompat.Builder {
    val view = RemoteViews(context.packageName, R.layout.notification_view)
    return NotificationCompat.Builder(context, "clinic_info")
        .apply {

            setContent(view)

            setSmallIcon(R.drawable.ic_launcher_foreground)

            priority = PRIORITY_MAX

            setCustomBigContentView(view)

            setAutoCancel(true)
        }
}