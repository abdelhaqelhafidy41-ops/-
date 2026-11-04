package com.example.core.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCalculatorTest {

    @Test
    fun testQiblaBearingsFromGlobalCities() {
        // 1. Medina (North of Mecca) -> should point almost due South (~177° to 178°)
        val bearingMedina = QiblaHelper.calculateQiblaBearing(24.4672, 39.6111)
        assertTrue(
            "Medina Qibla bearing should be approx 177.5°",
            bearingMedina in 176f..179f
        )

        // 2. Cairo (30.0444, 31.2357) -> should point South-East (~136°)
        val bearingCairo = QiblaHelper.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue(
            "Cairo Qibla bearing should be approx 136°",
            bearingCairo in 134f..138f
        )

        // 3. London (51.5074, -0.1278) -> should point South-East (~119°)
        val bearingLondon = QiblaHelper.calculateQiblaBearing(51.5074, -0.1278)
        assertTrue(
            "London Qibla bearing should be approx 119°",
            bearingLondon in 117f..121f
        )

        // 4. New York (40.7128, -74.0060) -> Great-circle route is ~58° (North-East)
        val bearingNewYork = QiblaHelper.calculateQiblaBearing(40.7128, -74.0060)
        assertTrue(
            "New York Qibla bearing should be approx 58°",
            bearingNewYork in 56f..60f
        )
    }

    @Test
    fun testDistanceToKaabaKm() {
        // Medina to Mecca is ~340 - 360 km
        val distanceMedina = QiblaHelper.calculateDistanceToKaabaKm(24.4672, 39.6111)
        assertTrue("Medina distance approx 350 km", distanceMedina in 330.0..370.0)

        // Cairo to Mecca is ~1280 km
        val distanceCairo = QiblaHelper.calculateDistanceToKaabaKm(30.0444, 31.2357)
        assertTrue("Cairo distance approx 1280 km", distanceCairo in 1200.0..1350.0)
    }

    @Test
    fun testHijriCalendarConversions() {
        val hijriDate = HijriCalendarHelper.getHijriDate()
        assertTrue("Hijri month must be between 1 and 12", hijriDate.month in 1..12)
        assertTrue("Hijri day must be between 1 and 30", hijriDate.day in 1..30)
        assertTrue("Hijri year must be >= 1445", hijriDate.year >= 1445)
        assertTrue("Formatted date must contain Hijri suffix", hijriDate.formattedArabic.contains("هـ"))
    }
}
