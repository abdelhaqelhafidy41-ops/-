package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val CHANNEL_PRAYER_ADHAN = "prayer_adhan_channel"
    const val CHANNEL_PRE_ADHAN = "prayer_pre_adhan_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val adhanChannel = NotificationChannel(
                CHANNEL_PRAYER_ADHAN,
                "تنبيهات الأذان ومواقيت الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات دخول وقت الصلاة والتكبير"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }

            val preAdhanChannel = NotificationChannel(
                CHANNEL_PRE_ADHAN,
                "تذكير قبل دخول وقت الصلاة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات الاستعداد للصلاة قبل الأذان بدقائق"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(adhanChannel)
            notificationManager.createNotificationChannel(preAdhanChannel)
        }
    }
}
