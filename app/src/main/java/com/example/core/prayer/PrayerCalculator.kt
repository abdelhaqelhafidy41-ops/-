package com.example.core.prayer

import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.Madhhab
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class PrayerSchedule(
    val fajr: Date,
    val sunrise: Date,
    val dhuhr: Date,
    val asr: Date,
    val maghrib: Date,
    val isha: Date,
    val date: Date,
    val cityName: String = "",
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhhab: Madhhab = Madhhab.STANDARD
)

data class NextPrayerInfo(
    val prayerNameArabic: String,
    val prayerTime: Date,
    val remainingMillis: Long,
    val prayerIndex: Int
)

data class PrayerOffsets(
    val fajr: Int = 0,
    val sunrise: Int = 0,
    val dhuhr: Int = 0,
    val asr: Int = 0,
    val maghrib: Int = 0,
    val isha: Int = 0
)

object PrayerCalculator {

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        date: Date = Date(),
        timeZone: TimeZone = TimeZone.getDefault(),
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        madhhab: Madhhab = Madhhab.STANDARD,
        offsets: PrayerOffsets = PrayerOffsets(),
        cityName: String = ""
    ): PrayerSchedule {
        val calendar = Calendar.getInstance(timeZone).apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tzOffset = timeZone.getOffset(date.time) / 3600000.0

        // 1. Julian Date
        val jd = julianDay(year, month, day) - longitude / (15.0 * 24.0)

        // 2. Solar coordinates (Meeus algorithm)
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0
        val decl = asinDeg(sinDeg(e) * sinDeg(l)) // Declination of the Sun
        val eqt = q / 15.0 - ra // Equation of Time in hours

        // 3. Solar Noon (Dhuhr) in local hours
        val noon = fixHour(12.0 + tzOffset - longitude / 15.0 - eqt)

        // 4. Sunrise & Sunset: sun center is at -0.833°
        val sunHourAngle = computeHourAngle(-0.833, latitude, decl)
        val sunriseHour = noon - sunHourAngle
        val sunsetHour = noon + sunHourAngle

        // 5. Fajr: sun altitude is -fajrAngle
        val fajrAngle = when (method) {
            CalculationMethod.UMM_AL_QURA -> 18.5
            CalculationMethod.MUSLIM_WORLD_LEAGUE -> 18.0
            CalculationMethod.EGYPTIAN -> 19.5
            CalculationMethod.KARACHI -> 18.0
            CalculationMethod.ISNA -> 15.0
        }
        val fajrHourAngle = computeHourAngle(-fajrAngle, latitude, decl)
        val fajrHour = noon - fajrHourAngle

        // 6. Asr: shadow length by Madhhab
        val shadowFactor = if (madhhab == Madhhab.HANAFI) 2.0 else 1.0
        val asrAlt = atanDeg(1.0 / (shadowFactor + tanDeg(Math.abs(latitude - decl))))
        val asrHourAngle = computeHourAngle(asrAlt, latitude, decl)
        val asrHour = noon + asrHourAngle

        // 7. Maghrib & Isha
        val maghribHour = sunsetHour
        val ishaHour: Double = when (method) {
            CalculationMethod.UMM_AL_QURA -> {
                maghribHour + 1.5 // 90 minutes after Maghrib
            }
            CalculationMethod.MUSLIM_WORLD_LEAGUE -> {
                noon + computeHourAngle(-17.0, latitude, decl)
            }
            CalculationMethod.EGYPTIAN -> {
                noon + computeHourAngle(-17.5, latitude, decl)
            }
            CalculationMethod.KARACHI -> {
                noon + computeHourAngle(-18.0, latitude, decl)
            }
            CalculationMethod.ISNA -> {
                noon + computeHourAngle(-15.0, latitude, decl)
            }
        }

        // Convert hours to Date objects and apply manual minute offsets
        val fajrDate = hourToDate(calendar, fajrHour, offsets.fajr, timeZone)
        val sunriseDate = hourToDate(calendar, sunriseHour, offsets.sunrise, timeZone)
        val dhuhrDate = hourToDate(calendar, noon, offsets.dhuhr, timeZone)
        val asrDate = hourToDate(calendar, asrHour, offsets.asr, timeZone)
        val maghribDate = hourToDate(calendar, maghribHour, offsets.maghrib, timeZone)
        val ishaDate = hourToDate(calendar, ishaHour, offsets.isha, timeZone)

        return PrayerSchedule(
            fajr = fajrDate,
            sunrise = sunriseDate,
            dhuhr = dhuhrDate,
            asr = asrDate,
            maghrib = maghribDate,
            isha = ishaDate,
            date = date,
            cityName = cityName,
            calculationMethod = method,
            madhhab = madhhab
        )
    }

    fun getNextPrayer(
        todaySchedule: PrayerSchedule,
        tomorrowSchedule: PrayerSchedule,
        now: Date = Date()
    ): NextPrayerInfo {
        val nowMillis = now.time

        return when {
            nowMillis < todaySchedule.fajr.time -> {
                NextPrayerInfo("الفجر", todaySchedule.fajr, todaySchedule.fajr.time - nowMillis, 0)
            }
            nowMillis < todaySchedule.sunrise.time -> {
                NextPrayerInfo("الشروق", todaySchedule.sunrise, todaySchedule.sunrise.time - nowMillis, 1)
            }
            nowMillis < todaySchedule.dhuhr.time -> {
                NextPrayerInfo("الظهر", todaySchedule.dhuhr, todaySchedule.dhuhr.time - nowMillis, 2)
            }
            nowMillis < todaySchedule.asr.time -> {
                NextPrayerInfo("العصر", todaySchedule.asr, todaySchedule.asr.time - nowMillis, 3)
            }
            nowMillis < todaySchedule.maghrib.time -> {
                NextPrayerInfo("المغرب", todaySchedule.maghrib, todaySchedule.maghrib.time - nowMillis, 4)
            }
            nowMillis < todaySchedule.isha.time -> {
                NextPrayerInfo("العشاء", todaySchedule.isha, todaySchedule.isha.time - nowMillis, 5)
            }
            else -> {
                NextPrayerInfo("فجر الغد", tomorrowSchedule.fajr, tomorrowSchedule.fajr.time - nowMillis, 6)
            }
        }
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun computeHourAngle(altitudeDeg: Double, latDeg: Double, declDeg: Double): Double {
        val num = sinDeg(altitudeDeg) - sinDeg(latDeg) * sinDeg(declDeg)
        val den = cosDeg(latDeg) * cosDeg(declDeg)
        val cosH = (num / den).coerceIn(-1.0, 1.0)
        return acosDeg(cosH) / 15.0
    }

    private fun hourToDate(baseCalendar: Calendar, fractionalHour: Double, offsetMinutes: Int, timeZone: TimeZone): Date {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = baseCalendar.timeInMillis
            val normalizedHour = fixHour(fractionalHour)
            val hour = floor(normalizedHour).toInt()
            val min = floor((normalizedHour - hour) * 60.0).toInt()
            val sec = floor(((normalizedHour - hour) * 60.0 - min) * 60.0).toInt()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, sec)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }
        return cal.time
    }

    private fun sinDeg(deg: Double): Double = sin(Math.toRadians(deg))
    private fun cosDeg(deg: Double): Double = cos(Math.toRadians(deg))
    private fun tanDeg(deg: Double): Double = tan(Math.toRadians(deg))
    private fun asinDeg(x: Double): Double = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double): Double = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atanDeg(x: Double): Double = Math.toDegrees(atan(x))
    private fun atan2Deg(y: Double, x: Double): Double = Math.toDegrees(atan2(y, x))

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h - 24.0 * floor(h / 24.0)
        if (res < 0) res += 24.0
        return res
    }
}
