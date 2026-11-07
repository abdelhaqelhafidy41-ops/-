package com.example.widgets

// 12. اتجاه القبلة والمسافة للكعبة المشرفة (3x2)
class QiblaWidgetProvider : BaseIslamicWidgetProvider()
typealias QiblaDirectionWidgetProvider = QiblaWidgetProvider
typealias QiblaCompassWidgetProvider = QiblaWidgetProvider

// 13. التاريخ والتقويم الهجري والميلادي (3x1)
class HijriDateWidgetProvider : BaseIslamicWidgetProvider()
typealias CombinedDateWidgetProvider = HijriDateWidgetProvider
typealias HijriTimeWidgetProvider = HijriDateWidgetProvider
typealias WeatherPrayerWidgetProvider = HijriDateWidgetProvider
typealias TravelPrayerStatusWidgetProvider = HijriDateWidgetProvider
typealias CityTimezoneWidgetProvider = HijriDateWidgetProvider

// 14. خطة العبادة والزاد اليومي الشامل (4x2)
class DailyWorshipPlanWidgetProvider : BaseIslamicWidgetProvider()
typealias TodaySummaryWidgetProvider = DailyWorshipPlanWidgetProvider

// 15. اختصارات الوصول السريع (4x1)
class QuickShortcutsWidgetProvider : BaseIslamicWidgetProvider()
