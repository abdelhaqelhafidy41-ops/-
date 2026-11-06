package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.NoorMuslimApp
import com.example.R
import com.example.core.prayer.HijriCalendarHelper
import com.example.core.prayer.PrayerCalculator
import com.example.core.prayer.PrayerSchedule
import com.example.core.prayer.QiblaHelper
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.Madhhab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WidgetUpdateCoordinator {

    private val timeFormat12 = SimpleDateFormat("hh:mm a", Locale("ar"))
    private val timeFormat24 = SimpleDateFormat("HH:mm", Locale.US)

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val app = context.applicationContext as? NoorMuslimApp
        val container = app?.container

        CoroutineScope(Dispatchers.IO).launch {
            val userPrefs = container?.userPreferencesDataStore?.userPreferencesFlow?.firstOrNull()
            val lat = userPrefs?.lastLatitude ?: 21.4225
            val lon = userPrefs?.lastLongitude ?: 39.8262
            val cityName = userPrefs?.lastCityName ?: "مكة المكرمة"
            val method = userPrefs?.calculationMethod ?: CalculationMethod.UMM_AL_QURA
            val madhhab = userPrefs?.madhhab ?: Madhhab.STANDARD

            val cal = Calendar.getInstance()
            val todaySchedule = PrayerCalculator.calculatePrayerTimes(
                latitude = lat,
                longitude = lon,
                date = cal.time,
                method = method,
                madhhab = madhhab,
                cityName = cityName
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowSchedule = PrayerCalculator.calculatePrayerTimes(
                latitude = lat,
                longitude = lon,
                date = cal.time,
                method = method,
                madhhab = madhhab,
                cityName = cityName
            )

            // Loop through all registered widget provider classes and update their views
            for (providerClass in allProviderClasses) {
                val component = ComponentName(context, providerClass)
                val widgetIds = appWidgetManager.getAppWidgetIds(component)
                for (id in widgetIds) {
                    val views = buildViewsForProvider(
                        context = context,
                        appWidgetId = id,
                        providerClass = providerClass,
                        todaySchedule = todaySchedule,
                        tomorrowSchedule = tomorrowSchedule,
                        lat = lat,
                        lon = lon,
                        cityName = cityName
                    )
                    appWidgetManager.updateAppWidget(id, views)
                }
            }
        }
    }

    suspend fun buildViewsForProvider(
        context: Context,
        appWidgetId: Int,
        providerClass: Class<*>,
        todaySchedule: PrayerSchedule,
        tomorrowSchedule: PrayerSchedule,
        lat: Double,
        lon: Double,
        cityName: String
    ): RemoteViews {
        val app = context.applicationContext as? NoorMuslimApp
        val container = app?.container

        val nextPrayer = PrayerCalculator.getNextPrayer(todaySchedule, tomorrowSchedule)
        val hijriDate = HijriCalendarHelper.getHijriDate()

        return when (providerClass.simpleName) {
            // 1. Prayer Family
            "PrayerNextWidgetProvider", "PrayerCountdownWidgetProvider", "PrayerFajrCountdownWidgetProvider", "PrayerHighlightCurrentWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_next)
                val isFajr = providerClass.simpleName == "PrayerFajrCountdownWidgetProvider"
                val prayerName = if (isFajr) "الفجر" else nextPrayer.prayerNameArabic
                val prayerTime = if (isFajr) timeFormat12.format(todaySchedule.fajr) else timeFormat12.format(nextPrayer.prayerTime)
                val countdownMs = if (isFajr) (todaySchedule.fajr.time - System.currentTimeMillis()).coerceAtLeast(0) else nextPrayer.remainingMillis

                views.setTextViewText(R.id.tv_widget_title, if (isFajr) "العد التنازلي للفجر 🌅" else "الصلاة القادمة 🕌")
                views.setTextViewText(R.id.tv_city, cityName)
                views.setTextViewText(R.id.tv_prayer_name, prayerName)
                views.setTextViewText(R.id.tv_prayer_time, prayerTime)
                views.setTextViewText(R.id.tv_countdown, formatRemaining(countdownMs))
                views.setTextViewText(R.id.tv_hijri_date, hijriDate.formattedArabic)
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "prayer"))
                views
            }

            "PrayerTimesDailyWidgetProvider", "PrayerTomorrowWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_daily)
                val schedule = if (providerClass.simpleName == "PrayerTomorrowWidgetProvider") tomorrowSchedule else todaySchedule
                val title = if (providerClass.simpleName == "PrayerTomorrowWidgetProvider") "مواقيت صلاة الغد 📅" else "مواقيت الصلاة اليوم 🕌"

                views.setTextViewText(R.id.tv_widget_title, title)
                views.setTextViewText(R.id.tv_city, cityName)
                views.setTextViewText(R.id.tv_fajr_time, timeFormat24.format(schedule.fajr))
                views.setTextViewText(R.id.tv_dhuhr_time, timeFormat24.format(schedule.dhuhr))
                views.setTextViewText(R.id.tv_asr_time, timeFormat24.format(schedule.asr))
                views.setTextViewText(R.id.tv_maghrib_time, timeFormat24.format(schedule.maghrib))
                views.setTextViewText(R.id.tv_isha_time, timeFormat24.format(schedule.isha))
                views.setTextViewText(R.id.tv_footer, "الصلاة القادمة: ${nextPrayer.prayerNameArabic} (${formatRemaining(nextPrayer.remainingMillis)})")
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "prayer"))
                views
            }

            "PrayerMiniWidgetProvider", "PrayerAdhanToggleWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_mini)
                views.setTextViewText(R.id.tv_mini_label, "الصلاة القادمة")
                views.setTextViewText(R.id.tv_mini_name, nextPrayer.prayerNameArabic)
                views.setTextViewText(R.id.tv_mini_time, timeFormat12.format(nextPrayer.prayerTime))
                views.setTextViewText(R.id.tv_mini_countdown, formatRemaining(nextPrayer.remainingMillis))
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "prayer"))
                views
            }

            "PrayerFajrSunriseWidgetProvider", "PrayerDhuhrAsrWidgetProvider", "PrayerMaghribIshaWidgetProvider", "PrayerRamadanImsakIftarWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_pair)
                when (providerClass.simpleName) {
                    "PrayerFajrSunriseWidgetProvider" -> {
                        views.setTextViewText(R.id.tv_pair_label1, "الفجر 🌅")
                        views.setTextViewText(R.id.tv_pair_time1, timeFormat12.format(todaySchedule.fajr))
                        views.setTextViewText(R.id.tv_pair_label2, "الشروق ☀️")
                        views.setTextViewText(R.id.tv_pair_time2, timeFormat12.format(todaySchedule.sunrise))
                    }
                    "PrayerDhuhrAsrWidgetProvider" -> {
                        views.setTextViewText(R.id.tv_pair_label1, "الظهر ☀️")
                        views.setTextViewText(R.id.tv_pair_time1, timeFormat12.format(todaySchedule.dhuhr))
                        views.setTextViewText(R.id.tv_pair_label2, "العصر 🌤️")
                        views.setTextViewText(R.id.tv_pair_time2, timeFormat12.format(todaySchedule.asr))
                    }
                    "PrayerMaghribIshaWidgetProvider" -> {
                        views.setTextViewText(R.id.tv_pair_label1, "المغرب 🌇")
                        views.setTextViewText(R.id.tv_pair_time1, timeFormat12.format(todaySchedule.maghrib))
                        views.setTextViewText(R.id.tv_pair_label2, "العشاء 🌌")
                        views.setTextViewText(R.id.tv_pair_time2, timeFormat12.format(todaySchedule.isha))
                    }
                    else -> { // Ramadan Imsak & Iftar
                        views.setTextViewText(R.id.tv_pair_label1, "الإمساك 🌙")
                        views.setTextViewText(R.id.tv_pair_time1, timeFormat12.format(todaySchedule.fajr))
                        views.setTextViewText(R.id.tv_pair_label2, "الإفطار 🌴")
                        views.setTextViewText(R.id.tv_pair_time2, timeFormat12.format(todaySchedule.maghrib))
                    }
                }
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "prayer"))
                views
            }

            // 2. Quran Family
            "QuranAyahOfDayWidgetProvider", "QuranLargeAyahWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_quran_ayah)
                val ayahDayList = listOf(
                    Triple("﴿ إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴾", "سورة الشرح - الآية ٦", "فإن مع المشقة سهولة ويسراً"),
                    Triple("﴿ وَقُل رَّبِّ زِدْنِي عِلْمًا ﴾", "سورة طه - الآية ١١٤", "طلب الاستزادة من العلم النافع"),
                    Triple("﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾", "سورة الرعد - الآية ٢٨", "سكينة النفس بذكر الله"),
                    Triple("﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ﴾", "سورة الطلاق - الآية ٢", "فرج وتيسير للمتقين")
                )
                val dayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt() % ayahDayList.size
                val item = ayahDayList[dayIndex]
                views.setTextViewText(R.id.tv_ayah_title, "آية اليوم 📖")
                views.setTextViewText(R.id.tv_ayah_text, item.first)
                views.setTextViewText(R.id.tv_ayah_surah, item.second)
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "quran"))
                views
            }

            "QuranLastReadWidgetProvider", "QuranResumeWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_quran_last_read)
                val progress = container?.database?.readingProgressDao()?.getReadingProgress()
                val surahName = progress?.surahNameArabic ?: "سورة البقرة"
                val ayahNum = progress?.ayahNumber ?: 1
                val pageNum = progress?.pageNumber ?: 1
                views.setTextViewText(R.id.tv_last_read_surah, surahName)
                views.setTextViewText(R.id.tv_last_read_ayah, "الآية رقم $ayahNum • صفحة $pageNum")
                val resumeIntent = createNavigationIntent(context, "quran_reading/${progress?.surahNumber ?: 2}")
                views.setOnClickPendingIntent(R.id.btn_resume_quran, resumeIntent)
                views.setOnClickPendingIntent(R.id.widget_root, resumeIntent)
                views
            }

            "QuranKhatmahProgressWidgetProvider", "QuranDailyWirdWidgetProvider", "QuranWirdRemainingWidgetProvider", "QuranMemorizationReminderWidgetProvider", "QuranMemorizationProgressWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_quran_khatmah)
                val khatmahs = container?.database?.khatmahDao()?.getAllKhatmahsList() ?: emptyList()
                val active = khatmahs.firstOrNull()
                val percent = if (active != null && active.totalTarget > 0) ((active.currentProgress.toDouble() / active.totalTarget) * 100).toInt() else 35
                val curr = active?.currentProgress ?: 210
                val total = active?.totalTarget ?: 604
                views.setTextViewText(R.id.tv_khatmah_title, active?.title ?: "ختمة القرآن الكريم 🌙")
                views.setTextViewText(R.id.tv_khatmah_percent, "$percent٪")
                views.setProgressBar(R.id.pb_khatmah, 100, percent, false)
                views.setTextViewText(R.id.tv_wird_detail, "المقروء: $curr من $total صفحة")
                views.setTextViewText(R.id.tv_wird_target, "المتبقي: ${total - curr} ص")
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "quran"))
                views
            }

            "QuranAudioPlayerWidgetProvider", "QuranMiniPlayerWidgetProvider", "QuranCurrentSurahWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_quran_player)
                val playerState = container?.audioPlayer?.state?.value
                val isPlaying = playerState?.isPlaying == true
                val surah = playerState?.currentSurahName ?: "سورة الكهف"
                val reciter = playerState?.reciter?.nameArabic ?: "مشاري راشد العفاسي"

                views.setTextViewText(R.id.tv_player_surah, surah)
                views.setTextViewText(R.id.tv_player_reciter, reciter)
                views.setTextViewText(R.id.btn_player_toggle, if (isPlaying) "⏸" else "▶")
                views.setOnClickPendingIntent(R.id.btn_player_toggle, createActionIntent(context, WidgetActionReceiver.ACTION_PLAYER_TOGGLE))
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "quran"))
                views
            }

            // 3. Adhkar & Tasbeeh Family
            "TasbeehCounterWidgetProvider", "TasbeehLargeCounterWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_adhkar_tasbeeh)
                val tasbeehState = container?.database?.dhikrDao()?.getTasbeehState()
                val phrase = tasbeehState?.phrase ?: "سبحان الله وبحمده"
                val count = tasbeehState?.count ?: 0
                val target = tasbeehState?.target ?: 33

                views.setTextViewText(R.id.tv_tasbeeh_phrase, phrase)
                views.setTextViewText(R.id.tv_tasbeeh_target, "الهدف: $target")
                views.setTextViewText(R.id.btn_tasbeeh_count, "$count")
                views.setOnClickPendingIntent(R.id.btn_tasbeeh_count, createActionIntent(context, WidgetActionReceiver.ACTION_TASBEEH_INCREMENT))
                views.setOnClickPendingIntent(R.id.btn_tasbeeh_reset, createActionIntent(context, WidgetActionReceiver.ACTION_TASBEEH_RESET))
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "adhkar"))
                views
            }

            "DhikrOfDayWidgetProvider", "DhikrMorningWidgetProvider", "DhikrEveningWidgetProvider", "DhikrAfterPrayerWidgetProvider", "DhikrLastCompletedWidgetProvider", "DhikrDailyPlanWidgetProvider", "DailyAchievementWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_adhkar_card)
                val isMorning = providerClass.simpleName == "DhikrMorningWidgetProvider"
                val isEvening = providerClass.simpleName == "DhikrEveningWidgetProvider"
                val isAfter = providerClass.simpleName == "DhikrAfterPrayerWidgetProvider"

                val categoryTitle = when {
                    isMorning -> "أذكار الصباح ☀️"
                    isEvening -> "أذكار المساء 🌙"
                    isAfter -> "أذكار ما بعد الصلاة 🕌"
                    else -> "ذكر اليوم والفضيلة 📿"
                }
                val dhikrText = when {
                    isMorning -> "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ"
                    isEvening -> "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ"
                    isAfter -> "أستغفر الله، أستغفر الله، أستغفر الله، اللهم أنت السلام ومنك السلام تباركت يا ذا الجلال والإكرام"
                    else -> "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ"
                }
                val virtue = when {
                    isMorning || isEvening -> "حصن المسلم من كل شر وحفظ طوال اليوم"
                    isAfter -> "سنة نبوية مؤكدة دبر كل صلاة مكتوبة"
                    else -> "كلمتان خفيفتان على اللسان ثقيلتان في الميزان"
                }

                views.setTextViewText(R.id.tv_dhikr_category, categoryTitle)
                views.setTextViewText(R.id.tv_dhikr_text, dhikrText)
                views.setTextViewText(R.id.tv_dhikr_reward, virtue)
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "adhkar"))
                views
            }

            // 4. Qibla & Utilities Family
            "QiblaDirectionWidgetProvider", "QiblaCompassWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_qibla)
                val bearing = QiblaHelper.calculateQiblaBearing(lat, lon)
                val dist = QiblaHelper.calculateDistanceToKaabaKm(lat, lon).toInt()
                views.setTextViewText(R.id.tv_qibla_angle, "${bearing.toInt()}° نحو الكعبة")
                views.setTextViewText(R.id.tv_qibla_distance, "المسافة للكعبة: %,d كم".format(dist))
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "prayer"))
                views
            }

            "HijriDateWidgetProvider", "CombinedDateWidgetProvider", "HijriTimeWidgetProvider", "WeatherPrayerWidgetProvider", "TravelPrayerStatusWidgetProvider", "CityTimezoneWidgetProvider", "DailyWorshipPlanWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_date_hijri)
                views.setTextViewText(R.id.tv_date_day_name, "يوم ${HijriCalendarHelper.getHijriDate().formattedArabic.split("،").firstOrNull() ?: "اليوم المبارك"}")
                views.setTextViewText(R.id.tv_date_hijri, hijriDate.formattedArabic)
                views.setTextViewText(R.id.tv_date_gregorian, HijriCalendarHelper.getFormattedGregorianDate())
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "home"))
                views
            }

            "TodaySummaryWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_summary)
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val achievement = container?.database?.dailyAchievementDao()?.getAchievementForDate(todayStr)
                val mask = achievement?.prayersCompletedMask ?: 0
                val completedPrayers = Integer.bitCount(mask).coerceIn(0, 5)
                val dhikrCount = achievement?.totalDhikrCount ?: 100
                val quranPages = ((achievement?.quranAyahsRead ?: 0) / 15).coerceAtLeast(1)
                views.setTextViewText(R.id.tv_summary_prayers, "$completedPrayers / 5")
                views.setTextViewText(R.id.tv_summary_dhikr, "$dhikrCount")
                views.setTextViewText(R.id.tv_summary_quran, "$quranPages ص")
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "statistics"))
                views
            }

            "QuickShortcutsWidgetProvider" -> {
                val views = RemoteViews(context.packageName, R.layout.widget_shortcuts)
                views.setOnClickPendingIntent(R.id.btn_shortcut_quran, createNavigationIntent(context, "quran"))
                views.setOnClickPendingIntent(R.id.btn_shortcut_adhkar, createNavigationIntent(context, "adhkar"))
                views.setOnClickPendingIntent(R.id.btn_shortcut_prayer, createNavigationIntent(context, "prayer"))
                views.setOnClickPendingIntent(R.id.btn_shortcut_qibla, createNavigationIntent(context, "prayer"))
                views
            }

            else -> {
                // Fallback default views
                val views = RemoteViews(context.packageName, R.layout.widget_prayer_next)
                views.setTextViewText(R.id.tv_widget_title, "نور المسلم")
                views.setTextViewText(R.id.tv_prayer_name, nextPrayer.prayerNameArabic)
                views.setTextViewText(R.id.tv_prayer_time, timeFormat12.format(nextPrayer.prayerTime))
                views.setOnClickPendingIntent(R.id.widget_root, createNavigationIntent(context, "home"))
                views
            }
        }
    }

    private fun formatRemaining(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = millis / (1000 * 60 * 60)
        return "متبقي %02d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
    }

    private fun createNavigationIntent(context: Context, route: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = WidgetActionReceiver.ACTION_OPEN_DESTINATION
            putExtra(WidgetActionReceiver.EXTRA_ROUTE, route)
        }
        return PendingIntent.getBroadcast(
            context,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    val allProviderClasses: List<Class<*>> = listOf(
        // Prayer Family (12)
        PrayerNextWidgetProvider::class.java,
        PrayerCountdownWidgetProvider::class.java,
        PrayerTimesDailyWidgetProvider::class.java,
        PrayerMiniWidgetProvider::class.java,
        PrayerFajrCountdownWidgetProvider::class.java,
        PrayerHighlightCurrentWidgetProvider::class.java,
        PrayerFajrSunriseWidgetProvider::class.java,
        PrayerDhuhrAsrWidgetProvider::class.java,
        PrayerMaghribIshaWidgetProvider::class.java,
        PrayerTomorrowWidgetProvider::class.java,
        PrayerRamadanImsakIftarWidgetProvider::class.java,
        PrayerAdhanToggleWidgetProvider::class.java,

        // Quran Family (12)
        QuranAyahOfDayWidgetProvider::class.java,
        QuranLargeAyahWidgetProvider::class.java,
        QuranLastReadWidgetProvider::class.java,
        QuranResumeWidgetProvider::class.java,
        QuranKhatmahProgressWidgetProvider::class.java,
        QuranDailyWirdWidgetProvider::class.java,
        QuranWirdRemainingWidgetProvider::class.java,
        QuranAudioPlayerWidgetProvider::class.java,
        QuranMiniPlayerWidgetProvider::class.java,
        QuranCurrentSurahWidgetProvider::class.java,
        QuranMemorizationReminderWidgetProvider::class.java,
        QuranMemorizationProgressWidgetProvider::class.java,

        // Adhkar Family (9)
        DhikrOfDayWidgetProvider::class.java,
        DhikrMorningWidgetProvider::class.java,
        DhikrEveningWidgetProvider::class.java,
        DhikrAfterPrayerWidgetProvider::class.java,
        TasbeehCounterWidgetProvider::class.java,
        TasbeehLargeCounterWidgetProvider::class.java,
        DhikrLastCompletedWidgetProvider::class.java,
        DhikrDailyPlanWidgetProvider::class.java,
        DailyAchievementWidgetProvider::class.java,

        // Qibla & Utilities Family (11)
        QiblaDirectionWidgetProvider::class.java,
        QiblaCompassWidgetProvider::class.java,
        HijriDateWidgetProvider::class.java,
        CombinedDateWidgetProvider::class.java,
        HijriTimeWidgetProvider::class.java,
        WeatherPrayerWidgetProvider::class.java,
        TravelPrayerStatusWidgetProvider::class.java,
        CityTimezoneWidgetProvider::class.java,
        DailyWorshipPlanWidgetProvider::class.java,
        TodaySummaryWidgetProvider::class.java,
        QuickShortcutsWidgetProvider::class.java
    )
}
