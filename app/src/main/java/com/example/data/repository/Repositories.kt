package com.example.data.repository

import com.example.data.importer.QuranDataImporter
import com.example.data.local.dao.DailyAchievementDao
import com.example.data.local.dao.DhikrDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.KhatmahDao
import com.example.data.local.dao.LibraryDao
import com.example.data.local.dao.MemorizationDao
import com.example.data.local.dao.PrayerTimeDao
import com.example.data.local.dao.QuranDao
import com.example.data.local.dao.ReadingProgressDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.DhikrHistoryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.IslamicLibraryEntity
import com.example.data.local.entity.KhatmahEntity
import com.example.data.local.entity.KhatmahLogEntity
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.entity.MemorizationQuizResultEntity
import com.example.data.local.entity.PrayerTimeEntity
import com.example.data.local.entity.QuranAyahEntity
import com.example.data.local.entity.QuranSurahEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.TasbeehStateEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferences
import com.example.data.local.preferences.UserPreferencesDataStore
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.local.dao.WallpaperDao
import com.example.data.local.dao.WidgetConfigDao
import com.example.data.local.entity.WallpaperEntity
import com.example.data.local.entity.WidgetConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun normalizeArabic(text: String): String {
    return text.replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
        .replace("أ", "ا")
        .replace("إ", "ا")
        .replace("آ", "ا")
        .replace("ة", "ه")
        .replace("ى", "ي")
}

// 1. Quran Repository
interface QuranRepository {
    fun getAllSurahs(): Flow<List<QuranSurahEntity>>
    fun searchSurahs(query: String): Flow<List<QuranSurahEntity>>
    suspend fun getSurah(surahNumber: Int): QuranSurahEntity?
    fun getAyahsForSurah(surahNumber: Int): Flow<List<QuranAyahEntity>>
    suspend fun getAyah(surahNumber: Int, ayahNumber: Int): QuranAyahEntity?
    suspend fun getAyahsRange(surahNumber: Int, startAyah: Int, endAyah: Int): List<QuranAyahEntity>
    fun searchAyahs(query: String): Flow<List<QuranAyahEntity>>
    suspend fun ensureInitialData()
    suspend fun downloadSurahAyahs(surahNumber: Int): List<QuranAyahEntity>
    suspend fun isSurahDownloaded(surahNumber: Int): Boolean
    fun getReadingProgress(): Flow<ReadingProgressEntity?>
    suspend fun saveReadingProgress(surahNumber: Int, surahName: String, ayahNumber: Int, page: Int, juz: Int)
    suspend fun clearReadingProgress()
}

class QuranRepositoryImpl(
    private val quranDao: QuranDao,
    private val readingProgressDao: ReadingProgressDao,
    private val dataImporter: QuranDataImporter
) : QuranRepository {
    override fun getAllSurahs(): Flow<List<QuranSurahEntity>> = quranDao.getAllSurahsFlow()

    override fun searchSurahs(query: String): Flow<List<QuranSurahEntity>> =
        if (query.isBlank()) quranDao.getAllSurahsFlow() else quranDao.searchSurahsFlow(query.trim())

    override suspend fun getSurah(surahNumber: Int): QuranSurahEntity? =
        quranDao.getSurahByNumber(surahNumber)

    override fun getAyahsForSurah(surahNumber: Int): Flow<List<QuranAyahEntity>> =
        quranDao.getAyahsForSurahFlow(surahNumber)

    override suspend fun getAyah(surahNumber: Int, ayahNumber: Int): QuranAyahEntity? =
        quranDao.getAyah(surahNumber, ayahNumber)

    override suspend fun getAyahsRange(surahNumber: Int, startAyah: Int, endAyah: Int): List<QuranAyahEntity> =
        quranDao.getAyahsRange(surahNumber, startAyah, endAyah)

    override fun searchAyahs(query: String): Flow<List<QuranAyahEntity>> =
        quranDao.searchAyahsFlow(query.trim())

    override suspend fun ensureInitialData() {
        dataImporter.seedInitialDataIfNeeded()
    }

    override suspend fun downloadSurahAyahs(surahNumber: Int): List<QuranAyahEntity> =
        dataImporter.downloadSurahAyahs(surahNumber)

    override suspend fun isSurahDownloaded(surahNumber: Int): Boolean {
        val surah = quranDao.getSurahByNumber(surahNumber) ?: return false
        val count = quranDao.getAyahsCountForSurah(surahNumber)
        return count >= surah.totalAyahs
    }

    override fun getReadingProgress(): Flow<ReadingProgressEntity?> =
        readingProgressDao.getReadingProgressFlow()

    override suspend fun saveReadingProgress(
        surahNumber: Int,
        surahName: String,
        ayahNumber: Int,
        page: Int,
        juz: Int
    ) {
        readingProgressDao.saveReadingProgress(
            ReadingProgressEntity(
                id = 1,
                surahNumber = surahNumber,
                surahNameArabic = surahName,
                ayahNumber = ayahNumber,
                pageNumber = page,
                juzNumber = juz,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearReadingProgress() {
        readingProgressDao.clearReadingProgress()
    }
}

// 2. Dhikr Repository
interface DhikrRepository {
    fun getDhikrByCategory(category: String): Flow<List<DhikrEntity>>
    fun getAllDhikr(): Flow<List<DhikrEntity>>
    fun searchDhikr(query: String): Flow<List<DhikrEntity>>
    fun getRecentHistory(): Flow<List<DhikrHistoryEntity>>
    fun getTodayDhikrCount(): Flow<Int?>
    suspend fun recordDhikrSession(title: String, count: Int, target: Int)
    suspend fun getDhikrCount(): Int
    suspend fun getDhikrById(id: Long): DhikrEntity?
    suspend fun insertDhikrItems(items: List<DhikrEntity>)
    fun getTasbeehState(): Flow<TasbeehStateEntity?>
    suspend fun saveTasbeehState(state: TasbeehStateEntity)
    suspend fun ensureInitialData()
}

class DhikrRepositoryImpl(
    private val dhikrDao: DhikrDao,
    private val dataImporter: com.example.data.importer.AdhkarDataImporter? = null
) : DhikrRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun ensureInitialData() {
        dataImporter?.seedInitialDataIfNeeded()
    }

    override fun getDhikrByCategory(category: String): Flow<List<DhikrEntity>> =
        dhikrDao.getDhikrByCategory(category)

    override fun getAllDhikr(): Flow<List<DhikrEntity>> = dhikrDao.getAllDhikr()

    override fun searchDhikr(query: String): Flow<List<DhikrEntity>> {
        val trimmed = query.trim()
        val normalized = normalizeArabic(trimmed)
        return dhikrDao.getAllDhikr().map { list ->
            if (trimmed.isEmpty()) list
            else list.filter { item ->
                normalizeArabic(item.title).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.arabicText).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.category).contains(normalized, ignoreCase = true) ||
                (item.reference != null && normalizeArabic(item.reference).contains(normalized, ignoreCase = true))
            }
        }
    }

    override fun getRecentHistory(): Flow<List<DhikrHistoryEntity>> =
        dhikrDao.getRecentDhikrHistory()

    override fun getTodayDhikrCount(): Flow<Int?> {
        val today = dateFormat.format(Date())
        return dhikrDao.getTodayTotalDhikrCountFlow(today)
    }

    override suspend fun recordDhikrSession(title: String, count: Int, target: Int) {
        val today = dateFormat.format(Date())
        dhikrDao.logDhikrSession(
            DhikrHistoryEntity(
                dhikrTitle = title,
                countDone = count,
                targetCount = target,
                dateString = today,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getDhikrCount(): Int = dhikrDao.getDhikrCount()

    override suspend fun getDhikrById(id: Long): DhikrEntity? = dhikrDao.getDhikrById(id)

    override suspend fun insertDhikrItems(items: List<DhikrEntity>) = dhikrDao.insertDhikrItems(items)

    override fun getTasbeehState(): Flow<TasbeehStateEntity?> = dhikrDao.getTasbeehStateFlow()

    override suspend fun saveTasbeehState(state: TasbeehStateEntity) = dhikrDao.saveTasbeehState(state)
}

// 3. Prayer Times Repository
interface PrayerTimesRepository {
    fun getPrayerTimesForToday(cityName: String): Flow<PrayerTimeEntity?>
    suspend fun savePrayerTimes(entity: PrayerTimeEntity)
}

class PrayerTimesRepositoryImpl(
    private val prayerTimeDao: PrayerTimeDao
) : PrayerTimesRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getPrayerTimesForToday(cityName: String): Flow<PrayerTimeEntity?> {
        val today = dateFormat.format(Date())
        return prayerTimeDao.getPrayerTimeFlow(today, cityName)
    }

    override suspend fun savePrayerTimes(entity: PrayerTimeEntity) {
        prayerTimeDao.insertPrayerTime(entity)
    }
}

// 4. Favorites Repository
interface FavoritesRepository {
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
    fun getFavoritesByType(itemType: String): Flow<List<FavoriteEntity>>
    fun isFavorite(itemType: String, itemId: String): Flow<Boolean>
    suspend fun toggleFavorite(
        itemType: String,
        itemId: String,
        title: String,
        subtitle: String? = null,
        arabicText: String? = null,
        reference: String? = null
    ): Boolean
}

class FavoritesRepositoryImpl(
    private val favoriteDao: FavoriteDao
) : FavoritesRepository {
    override fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    override fun getFavoritesByType(itemType: String): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavoritesByType(itemType)

    override fun isFavorite(itemType: String, itemId: String): Flow<Boolean> =
        favoriteDao.isFavoriteFlow(itemType, itemId)

    override suspend fun toggleFavorite(
        itemType: String,
        itemId: String,
        title: String,
        subtitle: String?,
        arabicText: String?,
        reference: String?
    ): Boolean {
        val currentlyFav = favoriteDao.isFavoriteFlow(itemType, itemId).firstOrNull() ?: false
        return if (currentlyFav) {
            favoriteDao.deleteFavorite(itemType, itemId)
            false
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    itemType = itemType,
                    itemId = itemId,
                    title = title,
                    subtitle = subtitle,
                    arabicText = arabicText,
                    reference = reference,
                    createdAt = System.currentTimeMillis()
                )
            )
            true
        }
    }
}

// 5. Daily Achievement Repository
interface AchievementRepository {
    fun getTodayAchievement(): Flow<DailyAchievementEntity?>
    suspend fun getTodayAchievementSync(): DailyAchievementEntity?
    suspend fun updateTodayAchievement(achievement: DailyAchievementEntity)
    suspend fun togglePrayerCompleted(prayerIndex: Int, completed: Boolean? = null)
    suspend fun logReadingProgress(ayahsRead: Int, minutesRead: Int)
    suspend fun logDhikrCount(count: Int)
    fun getLast30DaysAchievements(): Flow<List<DailyAchievementEntity>>
    fun getAllAchievementsFlow(): Flow<List<DailyAchievementEntity>>
    suspend fun calculateStreakDays(): Int
    suspend fun exportUserDataJson(): String
    suspend fun exportUserDataCsv(): String
    suspend fun clearAllUserData()
}

class AchievementRepositoryImpl(
    private val dailyAchievementDao: DailyAchievementDao,
    private val dhikrDao: DhikrDao? = null,
    private val favoriteDao: FavoriteDao? = null,
    private val readingProgressDao: ReadingProgressDao? = null,
    private val khatmahDao: KhatmahDao? = null,
    private val memorizationDao: MemorizationDao? = null,
    private val userPreferencesDataStore: UserPreferencesDataStore? = null
) : AchievementRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getTodayAchievement(): Flow<DailyAchievementEntity?> {
        val today = dateFormat.format(Date())
        return dailyAchievementDao.getAchievementForDateFlow(today)
    }

    override suspend fun getTodayAchievementSync(): DailyAchievementEntity? {
        val today = dateFormat.format(Date())
        return dailyAchievementDao.getAchievementForDate(today)
    }

    override suspend fun updateTodayAchievement(achievement: DailyAchievementEntity) {
        dailyAchievementDao.saveDailyAchievement(achievement)
    }

    override suspend fun togglePrayerCompleted(prayerIndex: Int, completed: Boolean?) {
        val today = dateFormat.format(Date())
        val current = dailyAchievementDao.getAchievementForDate(today) ?: DailyAchievementEntity(
            dateString = today,
            prayersCompletedMask = 0,
            quranMinutesRead = 0,
            quranAyahsRead = 0,
            totalDhikrCount = 0,
            morningDhikrCompleted = false,
            eveningDhikrCompleted = false
        )
        val bit = 1 shl prayerIndex.coerceIn(0, 4)
        val newMask = when (completed) {
            null -> current.prayersCompletedMask xor bit
            true -> current.prayersCompletedMask or bit
            false -> current.prayersCompletedMask and bit.inv()
        }
        dailyAchievementDao.saveDailyAchievement(current.copy(prayersCompletedMask = newMask))
    }

    override suspend fun logReadingProgress(ayahsRead: Int, minutesRead: Int) {
        val today = dateFormat.format(Date())
        val current = dailyAchievementDao.getAchievementForDate(today) ?: DailyAchievementEntity(
            dateString = today,
            prayersCompletedMask = 0,
            quranMinutesRead = 0,
            quranAyahsRead = 0,
            totalDhikrCount = 0,
            morningDhikrCompleted = false,
            eveningDhikrCompleted = false
        )
        dailyAchievementDao.saveDailyAchievement(
            current.copy(
                quranAyahsRead = current.quranAyahsRead + ayahsRead,
                quranMinutesRead = current.quranMinutesRead + minutesRead
            )
        )
    }

    override suspend fun logDhikrCount(count: Int) {
        val today = dateFormat.format(Date())
        val current = dailyAchievementDao.getAchievementForDate(today) ?: DailyAchievementEntity(
            dateString = today,
            prayersCompletedMask = 0,
            quranMinutesRead = 0,
            quranAyahsRead = 0,
            totalDhikrCount = 0,
            morningDhikrCompleted = false,
            eveningDhikrCompleted = false
        )
        dailyAchievementDao.saveDailyAchievement(
            current.copy(totalDhikrCount = current.totalDhikrCount + count)
        )
    }

    override fun getLast30DaysAchievements(): Flow<List<DailyAchievementEntity>> =
        dailyAchievementDao.getLast30DaysAchievements()

    override fun getAllAchievementsFlow(): Flow<List<DailyAchievementEntity>> =
        dailyAchievementDao.getAllAchievementsFlow()

    override suspend fun calculateStreakDays(): Int {
        val all = dailyAchievementDao.getAllAchievementsList()
        if (all.isEmpty()) return 0
        val map = all.associateBy { it.dateString }

        val checkCal = Calendar.getInstance()
        val todayKey = dateFormat.format(checkCal.time)
        val todayEntity = map[todayKey]
        val todayActive = todayEntity != null && (todayEntity.prayersCompletedMask > 0 || todayEntity.totalDhikrCount > 0 || todayEntity.quranAyahsRead > 0)

        var streak = 0
        if (todayActive) {
            streak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            val yesterdayCal = checkCal.clone() as Calendar
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = dateFormat.format(yesterdayCal.time)
            val yesterdayEntity = map[yesterdayKey]
            val yesterdayActive = yesterdayEntity != null && (yesterdayEntity.prayersCompletedMask > 0 || yesterdayEntity.totalDhikrCount > 0 || yesterdayEntity.quranAyahsRead > 0)
            if (!yesterdayActive) return 0
            checkCal.time = yesterdayCal.time
            streak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateKey = dateFormat.format(checkCal.time)
            val entity = map[dateKey]
            val active = entity != null && (entity.prayersCompletedMask > 0 || entity.totalDhikrCount > 0 || entity.quranAyahsRead > 0)
            if (active) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    override suspend fun exportUserDataJson(): String {
        val achievements = dailyAchievementDao.getAllAchievementsList()
        val dhikrHistory = dhikrDao?.getAllDhikrHistoryList() ?: emptyList()
        val favorites = favoriteDao?.getAllFavoritesList() ?: emptyList()
        val readingProgress = readingProgressDao?.getReadingProgress()
        val khatmahs = khatmahDao?.getAllKhatmahsList() ?: emptyList()
        val khatmahLogs = khatmahDao?.getAllKhatmahLogsList() ?: emptyList()
        val memorization = memorizationDao?.getAllMemorizationRecordsList() ?: emptyList()
        val quizResults = memorizationDao?.getAllQuizResultsList() ?: emptyList()
        val prefs = userPreferencesDataStore?.userPreferencesFlow?.firstOrNull()

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"app\": \"نور المسلم\",\n")
        sb.append("  \"exportTimestamp\": ${System.currentTimeMillis()},\n")
        sb.append("  \"exportDate\": \"${dateFormat.format(Date())}\",\n")
        sb.append("  \"achievementsCount\": ${achievements.size},\n")
        sb.append("  \"achievements\": [\n")
        achievements.forEachIndexed { i, a ->
            sb.append("    {\"date\": \"${a.dateString}\", \"prayersMask\": ${a.prayersCompletedMask}, \"prayersCount\": ${Integer.bitCount(a.prayersCompletedMask)}, \"quranAyahs\": ${a.quranAyahsRead}, \"quranMinutes\": ${a.quranMinutesRead}, \"dhikrCount\": ${a.totalDhikrCount}}")
            if (i < achievements.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"khatmahs\": [\n")
        khatmahs.forEachIndexed { i, k ->
            sb.append("    {\"id\": ${k.id}, \"title\": \"${k.title}\", \"targetDays\": ${k.targetDays}, \"progress\": ${k.currentProgress}, \"total\": ${k.totalTarget}, \"isCompleted\": ${k.isCompleted}}")
            if (i < khatmahs.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"memorizationRecords\": [\n")
        memorization.forEachIndexed { i, m ->
            sb.append("    {\"surah\": \"${m.surahName}\", \"ayahs\": \"${m.startAyah}-${m.endAyah}\", \"mastery\": ${m.masteryLevel}, \"reviews\": ${m.reviewCount}}")
            if (i < memorization.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ],\n")
        sb.append("  \"favoritesCount\": ${favorites.size},\n")
        sb.append("  \"readingProgress\": ${if (readingProgress != null) "{\"surah\": \"${readingProgress.surahNameArabic}\", \"page\": ${readingProgress.pageNumber}, \"juz\": ${readingProgress.juzNumber}}" else "null"}\n")
        sb.append("}")
        return sb.toString()
    }

    override suspend fun exportUserDataCsv(): String {
        val achievements = dailyAchievementDao.getAllAchievementsList()
        val sb = StringBuilder()
        sb.append("Date,PrayersCompletedCount,PrayersMask,QuranAyahsRead,QuranMinutesRead,TotalDhikrCount,MorningDhikrDone,EveningDhikrDone\n")
        achievements.forEach { a ->
            val count = Integer.bitCount(a.prayersCompletedMask)
            sb.append("${a.dateString},$count,${a.prayersCompletedMask},${a.quranAyahsRead},${a.quranMinutesRead},${a.totalDhikrCount},${a.morningDhikrCompleted},${a.eveningDhikrCompleted}\n")
        }
        return sb.toString()
    }

    override suspend fun clearAllUserData() {
        dailyAchievementDao.clearAllAchievements()
        dhikrDao?.clearDhikrHistory()
        dhikrDao?.clearTasbeehState()
        favoriteDao?.clearAllFavorites()
        readingProgressDao?.clearReadingProgress()
        khatmahDao?.clearAllKhatmahs()
        khatmahDao?.clearAllKhatmahLogs()
        memorizationDao?.clearAllMemorizationRecords()
        memorizationDao?.clearAllQuizResults()
    }
}

// 6. User Preferences Repository (Unified DataStore & Room snapshot)
interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateFontSizeScale(scale: FontSizeScale)
    suspend fun updateFontFamilyType(fontFamilyType: com.example.data.local.preferences.FontFamilyType)
    suspend fun updateLanguage(lang: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateCalculationMethod(method: CalculationMethod)
    suspend fun updateMadhhab(madhhab: Madhhab)
    suspend fun updateAudioPreference(audioPreference: AudioPreference)
    suspend fun updatePreferredReciter(reciterId: String)
    suspend fun updateVibrationEnabled(enabled: Boolean)
    suspend fun updateDailyDhikrReminderEnabled(enabled: Boolean)
    suspend fun updateLastLocation(latitude: Double, longitude: Double, cityName: String, hasPermission: Boolean)
    suspend fun updatePrayerNotification(prayerKey: String, enabled: Boolean)
    suspend fun updatePreAdhanMinutes(minutes: Int)
    suspend fun updateManualOffsets(fajr: Int, sunrise: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int)
    suspend fun resetToDefaults()
}

class UserPreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
    private val userSettingsDao: UserSettingsDao
) : UserPreferencesRepository {
    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.userPreferencesFlow

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.updateThemeMode(themeMode)
        syncToDatabase()
    }

    override suspend fun updateFontSizeScale(scale: FontSizeScale) {
        dataStore.updateFontSizeScale(scale)
        syncToDatabase()
    }

    override suspend fun updateFontFamilyType(fontFamilyType: com.example.data.local.preferences.FontFamilyType) {
        dataStore.updateFontFamilyType(fontFamilyType)
    }

    override suspend fun updateLanguage(lang: String) {
        dataStore.updateLanguage(lang)
        syncToDatabase()
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.updateNotificationsEnabled(enabled)
        syncToDatabase()
    }

    override suspend fun updateCalculationMethod(method: CalculationMethod) {
        dataStore.updateCalculationMethod(method)
        syncToDatabase()
    }

    override suspend fun updateMadhhab(madhhab: Madhhab) {
        dataStore.updateMadhhab(madhhab)
        syncToDatabase()
    }

    override suspend fun updateAudioPreference(audioPreference: AudioPreference) {
        dataStore.updateAudioPreference(audioPreference)
        syncToDatabase()
    }

    override suspend fun updatePreferredReciter(reciterId: String) {
        dataStore.updatePreferredReciter(reciterId)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.updateVibrationEnabled(enabled)
    }

    override suspend fun updateDailyDhikrReminderEnabled(enabled: Boolean) {
        dataStore.updateDailyDhikrReminderEnabled(enabled)
    }

    override suspend fun updateLastLocation(latitude: Double, longitude: Double, cityName: String, hasPermission: Boolean) {
        dataStore.updateLastLocation(latitude, longitude, cityName, hasPermission)
        syncToDatabase()
    }

    override suspend fun updatePrayerNotification(prayerKey: String, enabled: Boolean) {
        dataStore.updatePrayerNotification(prayerKey, enabled)
    }

    override suspend fun updatePreAdhanMinutes(minutes: Int) {
        dataStore.updatePreAdhanMinutes(minutes)
    }

    override suspend fun updateManualOffsets(fajr: Int, sunrise: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) {
        dataStore.updateManualOffsets(fajr, sunrise, dhuhr, asr, maghrib, isha)
    }

    override suspend fun resetToDefaults() {
        dataStore.resetToDefaults()
    }

    private suspend fun syncToDatabase() {
        val current = dataStore.userPreferencesFlow.firstOrNull() ?: return
        userSettingsDao.saveUserSettings(
            UserSettingsEntity(
                id = 1,
                themeMode = current.themeMode.name,
                fontSizeScale = current.fontSizeScale.scale,
                language = current.language,
                notificationsEnabled = current.notificationsEnabled,
                calculationMethod = current.calculationMethod.name,
                madhhab = current.madhhab.name,
                audioPreference = current.audioPreference.name,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}


// 7. Khatmah Repository
interface KhatmahRepository {
    fun getAllKhatmahs(): Flow<List<KhatmahEntity>>
    fun getActiveKhatmahs(): Flow<List<KhatmahEntity>>
    suspend fun getKhatmahById(id: Long): KhatmahEntity?
    suspend fun createKhatmah(
        title: String,
        targetDays: Int,
        targetType: String,
        totalTarget: Int
    ): Long
    suspend fun recordProgress(khatmahId: Long, additionalAmount: Int)
    suspend fun setKhatmahProgress(khatmahId: Long, currentProgress: Int)
    suspend fun togglePauseKhatmah(khatmahId: Long, isPaused: Boolean)
    suspend fun deleteKhatmah(khatmahId: Long)
    fun getKhatmahLogs(khatmahId: Long): Flow<List<KhatmahLogEntity>>
}

class KhatmahRepositoryImpl(
    private val khatmahDao: KhatmahDao
) : KhatmahRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getAllKhatmahs(): Flow<List<KhatmahEntity>> =
        khatmahDao.getAllKhatmahsFlow()

    override fun getActiveKhatmahs(): Flow<List<KhatmahEntity>> =
        khatmahDao.getActiveKhatmahsFlow()

    override suspend fun getKhatmahById(id: Long): KhatmahEntity? =
        khatmahDao.getKhatmahById(id)

    override suspend fun createKhatmah(
        title: String,
        targetDays: Int,
        targetType: String,
        totalTarget: Int
    ): Long {
        val cal = Calendar.getInstance()
        val startDate = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, targetDays)
        val targetEndDate = dateFormat.format(cal.time)

        val entity = KhatmahEntity(
            title = title,
            targetDays = targetDays,
            startDate = startDate,
            targetEndDate = targetEndDate,
            targetType = targetType,
            currentProgress = 0,
            totalTarget = totalTarget,
            isCompleted = false,
            isPaused = false,
            createdAt = System.currentTimeMillis()
        )
        return khatmahDao.insertKhatmah(entity)
    }

    override suspend fun recordProgress(khatmahId: Long, additionalAmount: Int) {
        val khatmah = khatmahDao.getKhatmahById(khatmahId) ?: return
        val newProgress = (khatmah.currentProgress + additionalAmount).coerceAtMost(khatmah.totalTarget)
        val isCompleted = newProgress >= khatmah.totalTarget

        khatmahDao.updateProgress(khatmahId, newProgress, isCompleted)

        val today = dateFormat.format(Date())
        khatmahDao.insertLog(
            KhatmahLogEntity(
                khatmahId = khatmahId,
                dateString = today,
                amountRead = additionalAmount,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun setKhatmahProgress(khatmahId: Long, currentProgress: Int) {
        val khatmah = khatmahDao.getKhatmahById(khatmahId) ?: return
        val clamped = currentProgress.coerceIn(0, khatmah.totalTarget)
        val isCompleted = clamped >= khatmah.totalTarget
        khatmahDao.updateProgress(khatmahId, clamped, isCompleted)
    }

    override suspend fun togglePauseKhatmah(khatmahId: Long, isPaused: Boolean) {
        khatmahDao.setPaused(khatmahId, isPaused)
    }

    override suspend fun deleteKhatmah(khatmahId: Long) {
        khatmahDao.deleteKhatmah(khatmahId)
    }

    override fun getKhatmahLogs(khatmahId: Long): Flow<List<KhatmahLogEntity>> =
        khatmahDao.getLogsForKhatmahFlow(khatmahId)
}

// 8. Memorization Repository
interface MemorizationRepository {
    fun getAllRecords(): Flow<List<MemorizationEntity>>
    fun getRecordsForSurah(surahNumber: Int): Flow<List<MemorizationEntity>>
    suspend fun addOrUpdateRecord(
        surahNumber: Int,
        surahName: String,
        startAyah: Int,
        endAyah: Int,
        masteryLevel: Int,
        nextReviewDays: Int,
        notes: String?
    ): Long
    suspend fun logReview(recordId: Long, newMasteryLevel: Int, nextReviewDays: Int)
    suspend fun deleteRecord(recordId: Long)
    suspend fun saveQuizResult(
        surahNumber: Int,
        surahName: String,
        totalQuestions: Int,
        correctAnswers: Int
    ): Long
    fun getRecentQuizResults(): Flow<List<MemorizationQuizResultEntity>>
}

class MemorizationRepositoryImpl(
    private val memorizationDao: MemorizationDao
) : MemorizationRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getAllRecords(): Flow<List<MemorizationEntity>> =
        memorizationDao.getAllRecordsFlow()

    override fun getRecordsForSurah(surahNumber: Int): Flow<List<MemorizationEntity>> =
        memorizationDao.getRecordsForSurahFlow(surahNumber)

    override suspend fun addOrUpdateRecord(
        surahNumber: Int,
        surahName: String,
        startAyah: Int,
        endAyah: Int,
        masteryLevel: Int,
        nextReviewDays: Int,
        notes: String?
    ): Long {
        val cal = Calendar.getInstance()
        val today = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, nextReviewDays)
        val nextReview = dateFormat.format(cal.time)

        val record = MemorizationEntity(
            surahNumber = surahNumber,
            surahName = surahName,
            startAyah = startAyah,
            endAyah = endAyah,
            masteryLevel = masteryLevel.coerceIn(1, 5),
            lastReviewedDate = today,
            nextReviewDate = nextReview,
            reviewCount = 1,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
        return memorizationDao.insertRecord(record)
    }

    override suspend fun logReview(recordId: Long, newMasteryLevel: Int, nextReviewDays: Int) {
        val record = memorizationDao.getRecordById(recordId) ?: return
        val cal = Calendar.getInstance()
        val today = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, nextReviewDays)
        val nextReview = dateFormat.format(cal.time)

        val updated = record.copy(
            masteryLevel = newMasteryLevel.coerceIn(1, 5),
            lastReviewedDate = today,
            nextReviewDate = nextReview,
            reviewCount = record.reviewCount + 1
        )
        memorizationDao.updateRecord(updated)
    }

    override suspend fun deleteRecord(recordId: Long) {
        memorizationDao.deleteRecord(recordId)
    }

    override suspend fun saveQuizResult(
        surahNumber: Int,
        surahName: String,
        totalQuestions: Int,
        correctAnswers: Int
    ): Long {
        val today = dateFormat.format(Date())
        val percentage = if (totalQuestions > 0) ((correctAnswers.toDouble() / totalQuestions) * 100).toInt() else 0
        return memorizationDao.insertQuizResult(
            MemorizationQuizResultEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                percentage = percentage,
                dateString = today,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override fun getRecentQuizResults(): Flow<List<MemorizationQuizResultEntity>> =
        memorizationDao.getRecentQuizResultsFlow()
}

// 9. Islamic Library Repository
interface LibraryRepository {
    fun getItemsByBook(bookId: String): Flow<List<IslamicLibraryEntity>>
    fun getAllItems(): Flow<List<IslamicLibraryEntity>>
    fun searchLibrary(query: String): Flow<List<IslamicLibraryEntity>>
    suspend fun getItemById(id: Long): IslamicLibraryEntity?
    suspend fun getLibraryCount(): Int
    suspend fun insertItems(items: List<IslamicLibraryEntity>)
}

class LibraryRepositoryImpl(
    private val libraryDao: LibraryDao
) : LibraryRepository {
    override fun getItemsByBook(bookId: String): Flow<List<IslamicLibraryEntity>> =
        libraryDao.getItemsByBookFlow(bookId)

    override fun getAllItems(): Flow<List<IslamicLibraryEntity>> =
        libraryDao.getAllItemsFlow()

    override fun searchLibrary(query: String): Flow<List<IslamicLibraryEntity>> {
        val trimmed = query.trim()
        val normalized = normalizeArabic(trimmed)
        return libraryDao.getAllItemsFlow().map { list ->
            if (trimmed.isEmpty()) list
            else list.filter { item ->
                normalizeArabic(item.textArabic).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.bookName).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.chapterName).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.author).contains(normalized, ignoreCase = true) ||
                normalizeArabic(item.reference).contains(normalized, ignoreCase = true)
            }
        }
    }

    override suspend fun getItemById(id: Long): IslamicLibraryEntity? =
        libraryDao.getItemById(id)

    override suspend fun getLibraryCount(): Int =
        libraryDao.getLibraryCount()

    override suspend fun insertItems(items: List<IslamicLibraryEntity>) =
        libraryDao.insertItems(items)
}

// 10. Wallpaper Repository
interface WallpaperRepository {
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>
    fun getWallpapersByCategory(category: String): Flow<List<WallpaperEntity>>
    fun searchWallpapers(query: String): Flow<List<WallpaperEntity>>
    fun getFavorites(): Flow<List<WallpaperEntity>>
    fun getDownloaded(): Flow<List<WallpaperEntity>>
    suspend fun getWallpaperById(id: String): WallpaperEntity?
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun downloadWallpaper(wallpaper: WallpaperEntity, context: Context): Result<String>
    suspend fun deleteLocalCopy(wallpaper: WallpaperEntity, context: Context)
    suspend fun applyWallpaper(context: Context, wallpaper: WallpaperEntity, target: Int): Result<Unit>
}

class WallpaperRepositoryImpl(
    private val wallpaperDao: WallpaperDao
) : WallpaperRepository {
    override fun getAllWallpapers(): Flow<List<WallpaperEntity>> =
        wallpaperDao.getAllWallpapersFlow()

    override fun getWallpapersByCategory(category: String): Flow<List<WallpaperEntity>> =
        wallpaperDao.getWallpapersByCategoryFlow(category)

    override fun searchWallpapers(query: String): Flow<List<WallpaperEntity>> {
        val trimmed = query.trim()
        val normalized = normalizeArabic(trimmed)
        return wallpaperDao.getAllWallpapersFlow().map { list ->
            if (trimmed.isEmpty()) list
            else list.filter { wp ->
                normalizeArabic(wp.titleArabic).contains(normalized, ignoreCase = true) ||
                (wp.titleEnglish != null && wp.titleEnglish.contains(trimmed, ignoreCase = true)) ||
                wp.category.contains(trimmed, ignoreCase = true) ||
                wp.designerOrAuthor.contains(trimmed, ignoreCase = true)
            }
        }
    }

    override fun getFavorites(): Flow<List<WallpaperEntity>> =
        wallpaperDao.getFavoritesFlow()

    override fun getDownloaded(): Flow<List<WallpaperEntity>> =
        wallpaperDao.getDownloadedFlow()

    override suspend fun getWallpaperById(id: String): WallpaperEntity? =
        wallpaperDao.getWallpaperById(id)

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        wallpaperDao.setFavorite(id, isFavorite)
    }

    override suspend fun downloadWallpaper(wallpaper: WallpaperEntity, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "wallpapers")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${wallpaper.id}.jpg")
            
            val url = URL(wallpaper.imageUrl)
            url.openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            val path = file.absolutePath
            wallpaperDao.setDownloaded(wallpaper.id, true, path)
            Result.success(path)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteLocalCopy(wallpaper: WallpaperEntity, context: Context) = withContext(Dispatchers.IO) {
        try {
            wallpaper.localFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
            wallpaperDao.deleteLocalFile(wallpaper.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun applyWallpaper(context: Context, wallpaper: WallpaperEntity, target: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bitmap: Bitmap = if (wallpaper.localFilePath != null && File(wallpaper.localFilePath).exists()) {
                BitmapFactory.decodeFile(wallpaper.localFilePath)
            } else {
                URL(wallpaper.imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } ?: return@withContext Result.failure(Exception("Could not decode bitmap"))

            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap, null, true, target)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

// 11. Widget Config Repository
interface WidgetConfigRepository {
    suspend fun getConfig(appWidgetId: Int): WidgetConfigEntity?
    fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfigEntity?>
    suspend fun getAllConfigs(): List<WidgetConfigEntity>
    suspend fun saveConfig(config: WidgetConfigEntity)
    suspend fun deleteConfig(appWidgetId: Int)
}

class WidgetConfigRepositoryImpl(
    private val widgetConfigDao: WidgetConfigDao
) : WidgetConfigRepository {
    override suspend fun getConfig(appWidgetId: Int): WidgetConfigEntity? =
        widgetConfigDao.getConfig(appWidgetId)

    override fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfigEntity?> =
        widgetConfigDao.getConfigFlow(appWidgetId)

    override suspend fun getAllConfigs(): List<WidgetConfigEntity> =
        widgetConfigDao.getAllConfigs()

    override suspend fun saveConfig(config: WidgetConfigEntity) =
        widgetConfigDao.saveConfig(config)

    override suspend fun deleteConfig(appWidgetId: Int) =
        widgetConfigDao.deleteConfig(appWidgetId)
}

