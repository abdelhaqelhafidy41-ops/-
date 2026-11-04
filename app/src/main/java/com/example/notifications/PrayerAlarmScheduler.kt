package com.example.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.prayer.PrayerCalculator
import com.example.core.prayer.PrayerOffsets
import com.example.data.local.preferences.UserPreferences
import java.util.Calendar
import java.util.Date

object PrayerAlarmScheduler {

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_IS_PRE_ADHAN = "extra_is_pre_adhan"
    const val EXTRA_CITY_NAME = "extra_city_name"
    const val EXTRA_MINUTES_LEFT = "extra_minutes_left"

    private const val REQUEST_CODE_BASE_ADHAN = 1000
    private const val REQUEST_CODE_BASE_PRE = 2000

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAllPrayerAlarms(context: Context, preferences: UserPreferences) {
        if (!preferences.notificationsEnabled) {
            cancelAllAlarms(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val offsets = PrayerOffsets(
            fajr = preferences.offsetFajr,
            sunrise = preferences.offsetSunrise,
            dhuhr = preferences.offsetDhuhr,
            asr = preferences.offsetAsr,
            maghrib = preferences.offsetMaghrib,
            isha = preferences.offsetIsha
        )

        // Schedule for Today and Tomorrow to guarantee no missed alarms across midnight
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = cal.time

        val todaySchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = preferences.lastLatitude,
            longitude = preferences.lastLongitude,
            date = today,
            method = preferences.calculationMethod,
            madhhab = preferences.madhhab,
            offsets = offsets,
            cityName = preferences.lastCityName
        )

        val tomorrowSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = preferences.lastLatitude,
            longitude = preferences.lastLongitude,
            date = tomorrow,
            method = preferences.calculationMethod,
            madhhab = preferences.madhhab,
            offsets = offsets,
            cityName = preferences.lastCityName
        )

        val prayersList = listOf(
            Triple("الفجر", todaySchedule.fajr, preferences.notifyFajr) to Triple("الفجر", tomorrowSchedule.fajr, preferences.notifyFajr),
            Triple("الشروق", todaySchedule.sunrise, preferences.notifySunrise) to Triple("الشروق", tomorrowSchedule.sunrise, preferences.notifySunrise),
            Triple("الظهر", todaySchedule.dhuhr, preferences.notifyDhuhr) to Triple("الظهر", tomorrowSchedule.dhuhr, preferences.notifyDhuhr),
            Triple("العصر", todaySchedule.asr, preferences.notifyAsr) to Triple("العصر", tomorrowSchedule.asr, preferences.notifyAsr),
            Triple("المغرب", todaySchedule.maghrib, preferences.notifyMaghrib) to Triple("المغرب", tomorrowSchedule.maghrib, preferences.notifyMaghrib),
            Triple("العشاء", todaySchedule.isha, preferences.notifyIsha) to Triple("العشاء", tomorrowSchedule.isha, preferences.notifyIsha)
        )

        val now = System.currentTimeMillis()

        prayersList.forEachIndexed { index, (todayItem, tomorrowItem) ->
            val targetItem = if (todayItem.second.time > now) todayItem else tomorrowItem
            val (name, timeDate, isEnabled) = targetItem

            val adhanRequestCode = REQUEST_CODE_BASE_ADHAN + index
            val preAdhanRequestCode = REQUEST_CODE_BASE_PRE + index

            if (isEnabled && timeDate.time > now) {
                // Schedule Adhan alarm
                val adhanIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra(EXTRA_PRAYER_NAME, name)
                    putExtra(EXTRA_IS_PRE_ADHAN, false)
                    putExtra(EXTRA_CITY_NAME, preferences.lastCityName)
                }
                val adhanPendingIntent = PendingIntent.getBroadcast(
                    context,
                    adhanRequestCode,
                    adhanIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                scheduleAlarm(alarmManager, timeDate.time, adhanPendingIntent)

                // Schedule Pre-adhan alarm if configured
                if (preferences.preAdhanMinutes > 0) {
                    val preTimeMillis = timeDate.time - (preferences.preAdhanMinutes * 60 * 1000L)
                    if (preTimeMillis > now) {
                        val preIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                            putExtra(EXTRA_PRAYER_NAME, name)
                            putExtra(EXTRA_IS_PRE_ADHAN, true)
                            putExtra(EXTRA_MINUTES_LEFT, preferences.preAdhanMinutes)
                            putExtra(EXTRA_CITY_NAME, preferences.lastCityName)
                        }
                        val prePendingIntent = PendingIntent.getBroadcast(
                            context,
                            preAdhanRequestCode,
                            preIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        scheduleAlarm(alarmManager, preTimeMillis, prePendingIntent)
                    }
                }
            } else {
                cancelAlarm(context, alarmManager, adhanRequestCode)
                cancelAlarm(context, alarmManager, preAdhanRequestCode)
            }
        }
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..10) {
            cancelAlarm(context, alarmManager, REQUEST_CODE_BASE_ADHAN + i)
            cancelAlarm(context, alarmManager, REQUEST_CODE_BASE_PRE + i)
        }
    }
}
