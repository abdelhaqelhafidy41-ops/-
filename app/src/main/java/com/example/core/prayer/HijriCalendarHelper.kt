package com.example.core.prayer

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameArabic: String,
    val formattedArabic: String
)

object HijriCalendarHelper {

    private val hijriMonthNames = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val arabicDayNames = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    /**
     * Converts a Gregorian Date to an authentic Hijri date based on Kuwaiti/Umm Al-Qura astronomical algorithm.
     */
    fun getHijriDate(date: Date = Date()): HijriDate {
        val cal = Calendar.getInstance().apply { time = date }
        var day = cal.get(Calendar.DAY_OF_MONTH)
        var month = cal.get(Calendar.MONTH) // 0-based
        var year = cal.get(Calendar.YEAR)

        var m = month + 1
        var y = year

        if (m < 3) {
            y -= 1
            m += 12
        }

        var a = floor(y / 100.0)
        var b = 2 - a + floor(a / 4.0)

        if (y < 1583) b = 0.0
        if (y == 1582) {
            if (m > 10) b = -10.0
            if (m == 10) {
                b = 0.0
                if (day > 4) b = -10.0
            }
        }

        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524

        b = 0.0
        if (jd > 2299160) {
            a = floor((jd - 1867216.25) / 36524.25)
            b = 1 + a - floor(a / 4.0)
        }

        val bb = jd + b + 1524
        var cc = floor((bb - 122.1) / 365.25)
        val dd = floor(365.25 * cc)
        val ee = floor((bb - dd) / 30.6001)

        val epochAstro = 1948084
        val epochCivil = 1948085

        val shift1 = 8.01 / 60.0

        var z = jd - epochAstro
        val cyc = floor(z / 10631.0)
        z -= 10631 * cyc
        val j = floor((z - shift1) / 354.366)
        val iy = 30 * cyc + j
        z -= floor(j * 354.366 + shift1)
        var im = floor((z + 28.5001) / 29.5)
        if (im == 13.0) im = 12.0
        val id = z - floor(im * 29.5001 - 29)

        val hDay = id.toInt().coerceIn(1, 30)
        val hMonth = im.toInt().coerceIn(1, 12)
        val hYear = iy.toInt()

        val monthName = hijriMonthNames.getOrElse(hMonth - 1) { "محرم" }
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        val dayName = arabicDayNames.getOrElse(dayOfWeekIndex) { "" }

        val formatted = "$dayName، $hDay $monthName $hYear هـ"

        return HijriDate(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameArabic = monthName,
            formattedArabic = formatted
        )
    }

    fun getFormattedGregorianDate(date: Date = Date()): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        return "${sdf.format(date)} م"
    }
}
