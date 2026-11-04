package com.example.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NoorMuslimApp
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val isPreAdhan = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_PRE_ADHAN, false)
        val cityName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_NAME) ?: ""
        val minutesLeft = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_LEFT, 10)

        NotificationChannels.createChannels(context)

        val channelId = if (isPreAdhan) {
            NotificationChannels.CHANNEL_PRE_ADHAN
        } else {
            NotificationChannels.CHANNEL_PRAYER_ADHAN
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPreAdhan) {
            "اقتراب موعد أذان $prayerName"
        } else {
            "أذان $prayerName"
        }

        val locationSuffix = if (cityName.isNotEmpty()) " بتوقيت $cityName" else ""
        val message = if (isPreAdhan) {
            "بقي $minutesLeft دقائق على دخول وقت صلاة $prayerName$locationSuffix. حي على الصلاة."
        } else {
            "حان الآن موعد أذان صلاة $prayerName$locationSuffix. الله أكبر، الله أكبر."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isPreAdhan) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (prayerName.hashCode() * 31) + if (isPreAdhan) 1 else 0
        notificationManager.notify(notificationId, notification)

        // Reschedule alarms for subsequent prayer times in background
        val app = context.applicationContext as? NoorMuslimApp
        val preferencesRepo = app?.container?.userPreferencesRepository
        if (preferencesRepo != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = preferencesRepo.userPreferencesFlow.firstOrNull()
                if (prefs != null) {
                    PrayerAlarmScheduler.scheduleAllPrayerAlarms(context, prefs)
                }
            }
        }
    }
}
