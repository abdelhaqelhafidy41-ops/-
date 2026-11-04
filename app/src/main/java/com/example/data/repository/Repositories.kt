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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override fun searchDhikr(query: String): Flow<List<DhikrEntity>> =
        dhikrDao.searchDhikr(query.trim())

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
    suspend fun updateTodayAchievement(achievement: DailyAchievementEntity)
}

class AchievementRepositoryImpl(
    private val dailyAchievementDao: DailyAchievementDao
) : AchievementRepository {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getTodayAchievement(): Flow<DailyAchievementEntity?> {
        val today = dateFormat.format(Date())
        return dailyAchievementDao.getAchievementForDateFlow(today)
    }

    override suspend fun updateTodayAchievement(achievement: DailyAchievementEntity) {
        dailyAchievementDao.saveDailyAchievement(achievement)
    }
}

// 6. User Preferences Repository (Unified DataStore & Room snapshot)
interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateFontSizeScale(scale: FontSizeScale)
    suspend fun updateLanguage(lang: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateCalculationMethod(method: CalculationMethod)
    suspend fun updateMadhhab(madhhab: Madhhab)
    suspend fun updateAudioPreference(audioPreference: AudioPreference)
    suspend fun updateLastLocation(latitude: Double, longitude: Double, cityName: String, hasPermission: Boolean)
    suspend fun updatePrayerNotification(prayerKey: String, enabled: Boolean)
    suspend fun updatePreAdhanMinutes(minutes: Int)
    suspend fun updateManualOffsets(fajr: Int, sunrise: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int)
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

    override fun searchLibrary(query: String): Flow<List<IslamicLibraryEntity>> =
        libraryDao.searchLibraryFlow(query.trim())

    override suspend fun getItemById(id: Long): IslamicLibraryEntity? =
        libraryDao.getItemById(id)

    override suspend fun getLibraryCount(): Int =
        libraryDao.getLibraryCount()

    override suspend fun insertItems(items: List<IslamicLibraryEntity>) =
        libraryDao.insertItems(items)
}

