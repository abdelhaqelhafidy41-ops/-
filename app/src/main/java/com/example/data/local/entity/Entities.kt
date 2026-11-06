package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 1. User Settings Entity (Stores database-backed user preferences & sync snapshot)
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM",
    val fontSizeScale: Float = 1.0f,
    val language: String = "ar",
    val notificationsEnabled: Boolean = false,
    val calculationMethod: String = "UMM_AL_QURA",
    val madhhab: String = "STANDARD",
    val audioPreference: String = "ADHAN",
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 2. Favorites Entity (Stores bookmarked verses, surahs, dhikr, duas)
 */
@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["itemType", "itemId"], unique = true)
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // "QURAN_AYAH", "SURAH", "DHIKR", "DUA"
    val itemId: String,
    val title: String,
    val subtitle: String? = null,
    val arabicText: String? = null,
    val reference: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 3. Reading Progress Entity (Stores last reading position in Quran)
 */
@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val surahNameArabic: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val juzNumber: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 4. Dhikr Category & Definition Entity (Catalog structure)
 */
@Entity(
    tableName = "dhikr_items",
    indices = [Index(value = ["category"])]
)
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "MORNING", "EVENING", "AFTER_PRAYER", "SLEEP", "WAKING_UP", "HOME", "MOSQUE", "TRAVEL", "RUQYAH", "QURANIC_DUA", "PROPHETS_DUA", "GENERAL_DUA"
    val title: String,
    val arabicText: String,
    val reference: String? = null,
    val grade: String? = "صحيح",
    val translation: String? = null,
    val repetitionTarget: Int = 33,
    val rewardNote: String? = null,
    val orderIndex: Int = 0
) {
    val targetCount: Int get() = repetitionTarget
    val virtue: String? get() = rewardNote
}

/**
 * 4b. Dhikr History Log Entity (Logs each completed dhikr/tasbeeh session)
 */
@Entity(
    tableName = "dhikr_history",
    indices = [Index(value = ["timestamp"])]
)
data class DhikrHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dhikrId: Long? = null,
    val dhikrTitle: String,
    val countDone: Int,
    val targetCount: Int,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 5. Daily Achievements Log Entity (Tracks daily prayer, reading, and dhikr habits)
 */
@Entity(
    tableName = "daily_achievements",
    indices = [Index(value = ["dateString"], unique = true)]
)
data class DailyAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val prayersCompletedMask: Int = 0, // Bitmask for Fajr, Dhuhr, Asr, Maghrib, Isha
    val quranMinutesRead: Int = 0,
    val quranAyahsRead: Int = 0,
    val totalDhikrCount: Int = 0,
    val morningDhikrCompleted: Boolean = false,
    val eveningDhikrCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 6. Saved Prayer Times Entity (Cached calculated / verified prayer times per city/day)
 */
@Entity(
    tableName = "saved_prayer_times",
    indices = [Index(value = ["dateString", "cityName"], unique = true)]
)
data class PrayerTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val fajrTime: String,
    val sunriseTime: String,
    val dhuhrTime: String,
    val asrTime: String,
    val maghribTime: String,
    val ishaTime: String,
    val calculationMethod: String,
    val savedAt: Long = System.currentTimeMillis()
)

/**
 * 7. Quran Surah Table Schema (Prepared structure for King Fahd Complex verified dataset)
 */
@Entity(tableName = "quran_surahs")
data class QuranSurahEntity(
    @PrimaryKey val surahNumber: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val revelationType: String, // "Meccan" or "Medinan"
    val totalAyahs: Int,
    val startPage: Int
)

/**
 * 7b. Quran Ayah Table Schema (Prepared structure without unverifiable mock religious text)
 */
@Entity(
    tableName = "quran_ayahs",
    indices = [
        Index(value = ["surahNumber", "ayahNumberInSurah"], unique = true),
        Index(value = ["pageNumber"])
    ]
)
data class QuranAyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val ayahNumberInSurah: Int,
    val ayahNumberOverall: Int,
    val textUthmani: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val sajda: Boolean = false
)

/**
 * 8. Khatmah Entity (Manages Quran Khatmah schedules, targets, and progress)
 */
@Entity(tableName = "khatmahs")
data class KhatmahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetDays: Int = 30,
    val startDate: String, // YYYY-MM-DD
    val targetEndDate: String, // YYYY-MM-DD
    val targetType: String = "PAGES", // "PAGES" (604 pages) or "AYAHS" (6236 ayahs)
    val currentProgress: Int = 0,
    val totalTarget: Int = 604,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val dailyPortion: Int
        get() = if (targetDays > 0) Math.ceil(totalTarget.toDouble() / targetDays).toInt() else 20

    val progressPercentage: Float
        get() = if (totalTarget > 0) (currentProgress.toFloat() / totalTarget.toFloat()) * 100f else 0f
}

/**
 * 8b. Khatmah Daily Reading Log Entity
 */
@Entity(
    tableName = "khatmah_logs",
    indices = [Index(value = ["khatmahId"])]
)
data class KhatmahLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val khatmahId: Long,
    val dateString: String, // YYYY-MM-DD
    val amountRead: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 9. Quran Memorization & Revision Tracking Entity
 */
@Entity(
    tableName = "memorization_records",
    indices = [Index(value = ["surahNumber", "startAyah", "endAyah"], unique = true)]
)
data class MemorizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val startAyah: Int,
    val endAyah: Int,
    val masteryLevel: Int = 3, // 1 to 5 stars (1: needs practice, 5: mastered)
    val lastReviewedDate: String, // YYYY-MM-DD
    val nextReviewDate: String, // YYYY-MM-DD
    val reviewCount: Int = 0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 9b. Memorization Quiz Results Entity
 */
@Entity(
    tableName = "memorization_quiz_results",
    indices = [Index(value = ["timestamp"])]
)
data class MemorizationQuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val surahName: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Int,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 10. Islamic Library Entity (Hadith collections, Riyad As-Salihin, Nawawi, Seerah)
 */
@Entity(
    tableName = "islamic_library_items",
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["chapterName"])
    ]
)
data class IslamicLibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String, // "BUKHARI", "MUSLIM", "NAWAWI_40", "RIYAD_SAL сосу", "HISN_ALMUSLIM", "SEERAH"
    val bookName: String,
    val author: String,
    val chapterName: String,
    val textArabic: String,
    val hadithNumber: String? = null,
    val grade: String? = "صحيح",
    val reference: String,
    val orderIndex: Int = 0
)

/**
 * 11. Saved Tasbeeh State Entity (Restores exact count, target, and selected phrase across app launches)
 */
@Entity(tableName = "tasbeeh_state")
data class TasbeehStateEntity(
    @PrimaryKey val id: Int = 1,
    val phrase: String = "سبحان الله وبحمده",
    val count: Int = 0,
    val target: Int = 33,
    val vibrationEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 12. Islamic Wallpaper Entity
 */
@Entity(
    tableName = "wallpapers",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isDownloaded"])
    ]
)
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val titleArabic: String,
    val titleEnglish: String? = null,
    val category: String, // mosques, kaaba, prophet_mosque, sky_stars, calligraphy, geometry, nature, sea_sunset, quran_verses, adhkar, dark, light, lock_screen, home_screen, ramadan, eid, minimal, tablet
    val imageUrl: String,
    val thumbnailUrl: String,
    val width: Int = 1080,
    val height: Int = 1920,
    val screenTarget: String = "both", // "home", "lock", "both"
    val source: String = "Unsplash",
    val designerOrAuthor: String = "Islamic Heritage / Open License",
    val licenseType: String = "Unsplash License",
    val attributionText: String = "Free for commercial and personal use",
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 13. App Widget Individual Configuration Entity
 */
@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey val appWidgetId: Int,
    val widgetType: String,
    val cityName: String = "مكة المكرمة",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val calculationMethod: String = "UMM_AL_QURA",
    val madhhab: String = "STANDARD",
    val themeMode: String = "SYSTEM",
    val showHijriDate: Boolean = true,
    val preferredReciter: String = "Alafasy_128kbps",
    val customPhrase: String = "سبحان الله وبحمده",
    val fontSizeMultiplier: Float = 1.0f,
    val lastUpdated: Long = System.currentTimeMillis()
)


