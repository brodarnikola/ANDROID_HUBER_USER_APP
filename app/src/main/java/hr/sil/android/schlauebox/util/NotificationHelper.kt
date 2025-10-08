package hr.sil.android.schlauebox.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.provider.Settings
import androidx.core.app.NotificationCompat
import hr.sil.android.schlauebox.App
import hr.sil.android.schlauebox.R


object NotificationHelper {

    var mNotificationManager: NotificationManager? = null

    init {
        mNotificationManager = App.ref.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    val NOTIFICATION_CHANNEL_ID = "10001"
    fun createNotification(title: String?, message: String?, klazz: Class<*>) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(App.ref, klazz)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(App.ref,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_IMMUTABLE)
        val mBuilder = NotificationCompat.Builder(App.ref, NOTIFICATION_CHANNEL_ID)

        val largeIcon = BitmapFactory.decodeResource(
                App.ref.resources,
                // it needs to have the same name in all flavours project,
                // so that push notification will work
                // tried to implement with flavors, but it does not want to work
                R.drawable.notification_icon_large
        )

        mBuilder.setSmallIcon(R.drawable.ic_mini_fab_spl)
        mBuilder.setContentTitle(title)
                .setLargeIcon(largeIcon)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(message))

        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
        mNotificationManager?.createNotificationChannel(notificationChannel)
        assert(mNotificationManager != null)
        mNotificationManager?.notify(0 /* Request Code */, mBuilder.build())
    }

    fun clearNotification() {
        if (mNotificationManager != null) {
            mNotificationManager?.cancelAll()
        }
    }

}