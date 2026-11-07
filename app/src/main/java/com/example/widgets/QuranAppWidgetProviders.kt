package com.example.widgets

// 6. آية اليوم وتدبر (4x2)
class AyahOfTheDayWidgetProvider : BaseIslamicWidgetProvider()
typealias QuranAyahOfDayWidgetProvider = AyahOfTheDayWidgetProvider
typealias QuranLargeAyahWidgetProvider = AyahOfTheDayWidgetProvider

// 7. متابعة قراءة القرآن والوصول السريع لآخر موضع (3x2)
class LastReadingWidgetProvider : BaseIslamicWidgetProvider()
typealias QuranLastReadWidgetProvider = LastReadingWidgetProvider
typealias QuranResumeWidgetProvider = LastReadingWidgetProvider

// 8. تقدم ختمة القرآن والورد اليومي (3x2)
class QuranProgressWidgetProvider : BaseIslamicWidgetProvider()
typealias QuranKhatmahProgressWidgetProvider = QuranProgressWidgetProvider
typealias QuranDailyWirdWidgetProvider = QuranProgressWidgetProvider
typealias QuranWirdRemainingWidgetProvider = QuranProgressWidgetProvider
typealias QuranMemorizationReminderWidgetProvider = QuranProgressWidgetProvider
typealias QuranMemorizationProgressWidgetProvider = QuranProgressWidgetProvider

// 9. مشغل القرآن الكريم (3x2)
class QuranPlayerWidgetProvider : BaseIslamicWidgetProvider()
typealias QuranAudioPlayerWidgetProvider = QuranPlayerWidgetProvider
typealias QuranMiniPlayerWidgetProvider = QuranPlayerWidgetProvider
typealias QuranCurrentSurahWidgetProvider = QuranPlayerWidgetProvider
