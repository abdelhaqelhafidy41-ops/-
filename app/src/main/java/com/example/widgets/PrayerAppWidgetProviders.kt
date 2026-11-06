package com.example.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

open class BaseIslamicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateCoordinator.updateAllWidgets(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateCoordinator.updateAllWidgets(context)
    }
}

// 1. الصلاة القادمة
class PrayerNextWidgetProvider : BaseIslamicWidgetProvider()

// 2. العد التنازلي للصلاة القادمة
class PrayerCountdownWidgetProvider : BaseIslamicWidgetProvider()

// 3. مواقيت الصلاة اليومية الكاملة
class PrayerTimesDailyWidgetProvider : BaseIslamicWidgetProvider()

// 4. ويدجت مصغر للصلاة
class PrayerMiniWidgetProvider : BaseIslamicWidgetProvider()

// 5. العد التنازلي لصلاة الفجر
class PrayerFajrCountdownWidgetProvider : BaseIslamicWidgetProvider()

// 6. الصلاة الحالية مع التمييز
class PrayerHighlightCurrentWidgetProvider : BaseIslamicWidgetProvider()

// 7. الفجر والشروق
class PrayerFajrSunriseWidgetProvider : BaseIslamicWidgetProvider()

// 8. الظهر والعصر
class PrayerDhuhrAsrWidgetProvider : BaseIslamicWidgetProvider()

// 9. المغرب والعشاء
class PrayerMaghribIshaWidgetProvider : BaseIslamicWidgetProvider()

// 10. مواقيت صلاة الغد
class PrayerTomorrowWidgetProvider : BaseIslamicWidgetProvider()

// 11. إمساك وإفطار رمضان
class PrayerRamadanImsakIftarWidgetProvider : BaseIslamicWidgetProvider()

// 12. حالة الأذان والتنبيهات
class PrayerAdhanToggleWidgetProvider : BaseIslamicWidgetProvider()
