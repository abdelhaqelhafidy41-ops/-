package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NoorMuslimApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PrayerBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? NoorMuslimApp
            val preferencesRepo = app?.container?.userPreferencesRepository ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val prefs = preferencesRepo.userPreferencesFlow.firstOrNull()
                if (prefs != null && prefs.notificationsEnabled) {
                    PrayerAlarmScheduler.scheduleAllPrayerAlarms(context, prefs)
                }
            }
        }
    }
}
