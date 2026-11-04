package com.example.core.prayer

import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.Madhhab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class PrayerCalculatorTest {

    @Test
    fun testPrayerCalculationDifferentLocations() {
        // 1. Mecca (21.4225, 39.8262) - Asia/Riyadh (UTC+3)
        val tzMecca = TimeZone.getTimeZone("Asia/Riyadh")
        val calMecca = Calendar.getInstance(tzMecca).apply {
            set(2026, Calendar.MAY, 15, 12, 0, 0)
        }
        val scheduleMecca = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = calMecca.time,
            timeZone = tzMecca,
            method = CalculationMethod.UMM_AL_QURA
        )

        assertNotNull(scheduleMecca.fajr)
        assertNotNull(scheduleMecca.sunrise)
        assertNotNull(scheduleMecca.dhuhr)
        assertNotNull(scheduleMecca.asr)
        assertNotNull(scheduleMecca.maghrib)
        assertNotNull(scheduleMecca.isha)

        // Verify logical chronological order
        assertTrue("Fajr before Sunrise", scheduleMecca.fajr.before(scheduleMecca.sunrise))
        assertTrue("Sunrise before Dhuhr", scheduleMecca.sunrise.before(scheduleMecca.dhuhr))
        assertTrue("Dhuhr before Asr", scheduleMecca.dhuhr.before(scheduleMecca.asr))
        assertTrue("Asr before Maghrib", scheduleMecca.asr.before(scheduleMecca.maghrib))
        assertTrue("Maghrib before Isha", scheduleMecca.maghrib.before(scheduleMecca.isha))

        // 2. Cairo (30.0444, 31.2357) - Africa/Cairo
        val tzCairo = TimeZone.getTimeZone("Africa/Cairo")
        val calCairo = Calendar.getInstance(tzCairo).apply {
            set(2026, Calendar.NOVEMBER, 4, 12, 0, 0)
        }
        val scheduleCairo = PrayerCalculator.calculatePrayerTimes(
            latitude = 30.0444,
            longitude = 31.2357,
            date = calCairo.time,
            timeZone = tzCairo,
            method = CalculationMethod.EGYPTIAN
        )
        assertNotNull(scheduleCairo.fajr)
        assertTrue(scheduleCairo.maghrib.before(scheduleCairo.isha))

        // 3. London (51.5074, -0.1278) - Europe/London
        val tzLondon = TimeZone.getTimeZone("Europe/London")
        val calLondon = Calendar.getInstance(tzLondon).apply {
            set(2026, Calendar.MARCH, 21, 12, 0, 0)
        }
        val scheduleLondon = PrayerCalculator.calculatePrayerTimes(
            latitude = 51.5074,
            longitude = -0.1278,
            date = calLondon.time,
            timeZone = tzLondon,
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )
        assertNotNull(scheduleLondon.dhuhr)
        assertTrue(scheduleLondon.fajr.before(scheduleLondon.sunrise))
    }

    @Test
    fun testMadhhabDifferencesForAsr() {
        val tz = TimeZone.getTimeZone("Asia/Riyadh")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.JUNE, 21, 12, 0, 0)
        }

        val shafiiSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.7136,
            longitude = 46.6753,
            date = cal.time,
            timeZone = tz,
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.STANDARD // Shadow factor 1
        )

        val hanafiSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.7136,
            longitude = 46.6753,
            date = cal.time,
            timeZone = tz,
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.HANAFI // Shadow factor 2
        )

        // Hanafi Asr is strictly later than Shafi'i Asr
        assertTrue(
            "Hanafi Asr must be later than Shafi'i Asr",
            hanafiSchedule.asr.after(shafiiSchedule.asr)
        )
        val diffMinutes = (hanafiSchedule.asr.time - shafiiSchedule.asr.time) / 60000
        assertTrue("Difference between Shafi'i and Hanafi Asr should be substantial (>30 min)", diffMinutes > 30)
    }

    @Test
    fun testCalculationMethodsFajrAndIsha() {
        val tz = TimeZone.getTimeZone("Africa/Cairo")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }

        // Egyptian (19.5° Fajr) vs ISNA (15° Fajr)
        val egyptianSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 30.0444,
            longitude = 31.2357,
            date = cal.time,
            timeZone = tz,
            method = CalculationMethod.EGYPTIAN
        )

        val isnaSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 30.0444,
            longitude = 31.2357,
            date = cal.time,
            timeZone = tz,
            method = CalculationMethod.ISNA
        )

        // 19.5° Fajr starts earlier than 15° Fajr
        assertTrue(
            "19.5° Egyptian Fajr must be earlier than 15° ISNA Fajr",
            egyptianSchedule.fajr.before(isnaSchedule.fajr)
        )
    }

    @Test
    fun testNextPrayerAfterMidnight() {
        val tz = TimeZone.getTimeZone("Asia/Riyadh")
        val calToday = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 4, 12, 0, 0)
        }
        val calTomorrow = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 5, 12, 0, 0)
        }

        val todaySchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = calToday.time,
            timeZone = tz
        )

        val tomorrowSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = calTomorrow.time,
            timeZone = tz
        )

        // Simulate time at 11:30 PM (after Isha of today)
        val afterIshaTime = Calendar.getInstance(tz).apply {
            time = todaySchedule.isha
            add(Calendar.HOUR_OF_DAY, 2)
        }.time

        val nextPrayer = PrayerCalculator.getNextPrayer(todaySchedule, tomorrowSchedule, afterIshaTime)

        assertEquals("فجر الغد", nextPrayer.prayerNameArabic)
        assertEquals(6, nextPrayer.prayerIndex)
        assertTrue(nextPrayer.remainingMillis > 0)
        assertEquals(tomorrowSchedule.fajr.time, nextPrayer.prayerTime.time)
    }

    @Test
    fun testManualMinuteOffsets() {
        val tz = TimeZone.getTimeZone("Asia/Riyadh")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 4, 12, 0, 0)
        }

        val baseSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.7136,
            longitude = 46.6753,
            date = cal.time,
            timeZone = tz,
            offsets = PrayerOffsets(fajr = 0, dhuhr = 0)
        )

        val offsetSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.7136,
            longitude = 46.6753,
            date = cal.time,
            timeZone = tz,
            offsets = PrayerOffsets(fajr = 5, dhuhr = -3)
        )

        val fajrDiff = (offsetSchedule.fajr.time - baseSchedule.fajr.time) / 60000
        val dhuhrDiff = (offsetSchedule.dhuhr.time - baseSchedule.dhuhr.time) / 60000

        assertEquals(5L, fajrDiff)
        assertEquals(-3L, dhuhrDiff)
    }

    @Test
    fun testTimezoneChange() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 1, 12, 0, 0)
        }

        val tz1 = TimeZone.getTimeZone("UTC")
        val tz2 = TimeZone.getTimeZone("GMT+3")

        val scheduleUtc = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = cal.time,
            timeZone = tz1
        )

        val schedulePlus3 = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = cal.time,
            timeZone = tz2
        )

        // The absolute physical instant of solar noon is identical regardless of timezone representation
        val diffMillis = Math.abs(scheduleUtc.dhuhr.time - schedulePlus3.dhuhr.time)
        assertTrue("Dhuhr UTC timestamp must match regardless of timezone ($diffMillis ms)", diffMillis < 60000)

        // Local clock in GMT+3 is 3 hours ahead of local clock in UTC
        val calUtc = Calendar.getInstance(tz1).apply { time = scheduleUtc.dhuhr }
        val calPlus3 = Calendar.getInstance(tz2).apply { time = schedulePlus3.dhuhr }
        assertEquals(3, calPlus3.get(Calendar.HOUR_OF_DAY) - calUtc.get(Calendar.HOUR_OF_DAY))
    }
}
