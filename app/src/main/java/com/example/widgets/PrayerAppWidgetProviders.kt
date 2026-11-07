package com.example.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

open class BaseIslamicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateCoordinator.updateWidgetIds(context, appWidgetManager, appWidgetIds, this::class.java)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateCoordinator.updateAllWidgets(context)
    }
}

// 1. الصلاة القادمة والعد التنازلي (3x2)
class NextPrayerWidgetProvider : BaseIslamicWidgetProvider()
typealias PrayerNextWidgetProvider = NextPrayerWidgetProvider
typealias PrayerCountdownWidgetProvider = NextPrayerWidgetProvider
typealias PrayerFajrCountdownWidgetProvider = NextPrayerWidgetProvider
typealias PrayerHighlightCurrentWidgetProvider = NextPrayerWidgetProvider

// 2. مواقيت الصلاة اليومية الكاملة (4x2)
class PrayerTimesDailyWidgetProvider : BaseIslamicWidgetProvider()
typealias PrayerTomorrowWidgetProvider = PrayerTimesDailyWidgetProvider

// 3. ويدجت مصغر للصلاة القادمة (2x1)
class PrayerMiniWidgetProvider : BaseIslamicWidgetProvider()
typealias PrayerAdhanToggleWidgetProvider = PrayerMiniWidgetProvider

// 4. الفجر والشروق / الصلوات الثنائية (3x1)
class PrayerPairWidgetProvider : BaseIslamicWidgetProvider()
typealias PrayerFajrSunriseWidgetProvider = PrayerPairWidgetProvider
typealias PrayerDhuhrAsrWidgetProvider = PrayerPairWidgetProvider
typealias PrayerMaghribIshaWidgetProvider = PrayerPairWidgetProvider

// 5. إمساك وإفطار رمضان (3x2)
class RamadanPrayerWidgetProvider : BaseIslamicWidgetProvider()
typealias PrayerRamadanImsakIftarWidgetProvider = RamadanPrayerWidgetProvider
