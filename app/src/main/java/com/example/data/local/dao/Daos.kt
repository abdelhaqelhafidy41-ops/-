package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.local.entity.WallpaperEntity
import com.example.data.local.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE itemType = :itemType ORDER BY createdAt DESC")
    fun getFavoritesByType(itemType: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemType = :itemType AND itemId = :itemId)")
    fun isFavoriteFlow(itemType: String, itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE itemType = :itemType AND itemId = :itemId")
    suspend fun deleteFavorite(itemType: String, itemId: String)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    suspend fun getAllFavoritesList(): List<FavoriteEntity>
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    fun getReadingProgressFlow(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id = 1 LIMIT 1")
    suspend fun getReadingProgress(): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE id = 1")
    suspend fun clearReadingProgress()
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_items WHERE category = :category ORDER BY orderIndex ASC")
    fun getDhikrByCategory(category: String): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr_items ORDER BY orderIndex ASC")
    fun getAllDhikr(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr_items WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' ORDER BY orderIndex ASC")
    fun searchDhikr(query: String): Flow<List<DhikrEntity>>

    @Query("SELECT COUNT(*) FROM dhikr_items")
    suspend fun getDhikrCount(): Int

    @Query("SELECT * FROM dhikr_items WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: Long): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrItems(items: List<DhikrEntity>)

    // History & logs
    @Query("SELECT * FROM dhikr_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDhikrHistory(limit: Int = 50): Flow<List<DhikrHistoryEntity>>

    @Query("SELECT SUM(countDone) FROM dhikr_history WHERE dateString = :dateString")
    fun getTodayTotalDhikrCountFlow(dateString: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logDhikrSession(history: DhikrHistoryEntity): Long

    // Persistent Tasbeeh State across launches
    @Query("SELECT * FROM tasbeeh_state WHERE id = 1 LIMIT 1")
    fun getTasbeehStateFlow(): Flow<TasbeehStateEntity?>

    @Query("SELECT * FROM tasbeeh_state WHERE id = 1 LIMIT 1")
    suspend fun getTasbeehState(): TasbeehStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasbeehState(state: TasbeehStateEntity)

    @Query("DELETE FROM dhikr_history")
    suspend fun clearDhikrHistory()

    @Query("DELETE FROM tasbeeh_state")
    suspend fun clearTasbeehState()

    @Query("SELECT * FROM dhikr_history ORDER BY timestamp ASC")
    suspend fun getAllDhikrHistoryList(): List<DhikrHistoryEntity>
}

@Dao
interface DailyAchievementDao {
    @Query("SELECT * FROM daily_achievements WHERE dateString = :dateString LIMIT 1")
    fun getAchievementForDateFlow(dateString: String): Flow<DailyAchievementEntity?>

    @Query("SELECT * FROM daily_achievements WHERE dateString = :dateString LIMIT 1")
    suspend fun getAchievementForDate(dateString: String): DailyAchievementEntity?

    @Query("SELECT * FROM daily_achievements ORDER BY dateString DESC LIMIT 30")
    fun getLast30DaysAchievements(): Flow<List<DailyAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyAchievement(achievement: DailyAchievementEntity)

    @Query("DELETE FROM daily_achievements")
    suspend fun clearAllAchievements()

    @Query("SELECT * FROM daily_achievements ORDER BY dateString ASC")
    suspend fun getAllAchievementsList(): List<DailyAchievementEntity>

    @Query("SELECT * FROM daily_achievements ORDER BY dateString ASC")
    fun getAllAchievementsFlow(): Flow<List<DailyAchievementEntity>>
}

@Dao
interface PrayerTimeDao {
    @Query("SELECT * FROM saved_prayer_times WHERE dateString = :dateString AND cityName = :cityName LIMIT 1")
    fun getPrayerTimeFlow(dateString: String, cityName: String): Flow<PrayerTimeEntity?>

    @Query("SELECT * FROM saved_prayer_times WHERE dateString = :dateString ORDER BY savedAt DESC LIMIT 1")
    fun getLatestPrayerTimeForDateFlow(dateString: String): Flow<PrayerTimeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTimeEntity)

    @Query("DELETE FROM saved_prayer_times WHERE dateString < :olderThanDate")
    suspend fun deleteOldPrayerTimes(olderThanDate: String)
}

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_surahs ORDER BY surahNumber ASC")
    fun getAllSurahsFlow(): Flow<List<QuranSurahEntity>>

    @Query("SELECT * FROM quran_surahs WHERE surahNumber = :surahNumber LIMIT 1")
    suspend fun getSurahByNumber(surahNumber: Int): QuranSurahEntity?

    @Query("SELECT * FROM quran_surahs WHERE nameArabic LIKE '%' || :query || '%' OR nameEnglish LIKE '%' || :query || '%' OR CAST(surahNumber AS TEXT) = :query ORDER BY surahNumber ASC")
    fun searchSurahsFlow(query: String): Flow<List<QuranSurahEntity>>

    @Query("SELECT COUNT(*) FROM quran_surahs")
    suspend fun getSurahsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<QuranSurahEntity>)

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumberInSurah ASC")
    fun getAyahsForSurahFlow(surahNumber: Int): Flow<List<QuranAyahEntity>>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumberInSurah ASC")
    suspend fun getAyahsForSurah(surahNumber: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND ayahNumberInSurah = :ayahNumber LIMIT 1")
    suspend fun getAyah(surahNumber: Int, ayahNumber: Int): QuranAyahEntity?

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber AND ayahNumberInSurah BETWEEN :startAyah AND :endAyah ORDER BY ayahNumberInSurah ASC")
    suspend fun getAyahsRange(surahNumber: Int, startAyah: Int, endAyah: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE textUthmani LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumberInSurah ASC LIMIT 100")
    fun searchAyahsFlow(query: String): Flow<List<QuranAyahEntity>>

    @Query("SELECT COUNT(*) FROM quran_ayahs")
    suspend fun getAyahsCount(): Int

    @Query("SELECT COUNT(*) FROM quran_ayahs WHERE surahNumber = :surahNumber")
    suspend fun getAyahsCountForSurah(surahNumber: Int): Int

    @Query("DELETE FROM quran_ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsForSurah(surahNumber: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<QuranAyahEntity>)
}

@Dao
interface KhatmahDao {
    @Query("SELECT * FROM khatmahs ORDER BY createdAt DESC")
    fun getAllKhatmahsFlow(): Flow<List<KhatmahEntity>>

    @Query("SELECT * FROM khatmahs WHERE isCompleted = 0 AND isPaused = 0 ORDER BY createdAt DESC")
    fun getActiveKhatmahsFlow(): Flow<List<KhatmahEntity>>

    @Query("SELECT * FROM khatmahs WHERE id = :id LIMIT 1")
    suspend fun getKhatmahById(id: Long): KhatmahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhatmah(khatmah: KhatmahEntity): Long

    @Update
    suspend fun updateKhatmah(khatmah: KhatmahEntity)

    @Query("DELETE FROM khatmahs WHERE id = :id")
    suspend fun deleteKhatmah(id: Long)

    @Query("UPDATE khatmahs SET currentProgress = :progress, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, isCompleted: Boolean)

    @Query("UPDATE khatmahs SET isPaused = :isPaused WHERE id = :id")
    suspend fun setPaused(id: Long, isPaused: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: KhatmahLogEntity): Long

    @Query("SELECT * FROM khatmah_logs WHERE khatmahId = :khatmahId ORDER BY timestamp DESC")
    fun getLogsForKhatmahFlow(khatmahId: Long): Flow<List<KhatmahLogEntity>>

    @Query("DELETE FROM khatmahs")
    suspend fun clearAllKhatmahs()

    @Query("DELETE FROM khatmah_logs")
    suspend fun clearAllKhatmahLogs()

    @Query("SELECT * FROM khatmahs ORDER BY createdAt DESC")
    suspend fun getAllKhatmahsList(): List<KhatmahEntity>

    @Query("SELECT * FROM khatmah_logs ORDER BY timestamp DESC")
    suspend fun getAllKhatmahLogsList(): List<KhatmahLogEntity>
}

@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorization_records ORDER BY surahNumber ASC, startAyah ASC")
    fun getAllRecordsFlow(): Flow<List<MemorizationEntity>>

    @Query("SELECT * FROM memorization_records WHERE surahNumber = :surahNumber ORDER BY startAyah ASC")
    fun getRecordsForSurahFlow(surahNumber: Int): Flow<List<MemorizationEntity>>

    @Query("SELECT * FROM memorization_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): MemorizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MemorizationEntity): Long

    @Update
    suspend fun updateRecord(record: MemorizationEntity)

    @Query("DELETE FROM memorization_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: MemorizationQuizResultEntity): Long

    @Query("SELECT * FROM memorization_quiz_results ORDER BY timestamp DESC LIMIT 50")
    fun getRecentQuizResultsFlow(): Flow<List<MemorizationQuizResultEntity>>

    @Query("DELETE FROM memorization_records")
    suspend fun clearAllMemorizationRecords()

    @Query("DELETE FROM memorization_quiz_results")
    suspend fun clearAllQuizResults()

    @Query("SELECT * FROM memorization_records ORDER BY createdAt DESC")
    suspend fun getAllMemorizationRecordsList(): List<MemorizationEntity>

    @Query("SELECT * FROM memorization_quiz_results ORDER BY timestamp DESC")
    suspend fun getAllQuizResultsList(): List<MemorizationQuizResultEntity>
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM islamic_library_items WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getItemsByBookFlow(bookId: String): Flow<List<IslamicLibraryEntity>>

    @Query("SELECT * FROM islamic_library_items ORDER BY orderIndex ASC")
    fun getAllItemsFlow(): Flow<List<IslamicLibraryEntity>>

    @Query("SELECT * FROM islamic_library_items WHERE textArabic LIKE '%' || :query || '%' OR bookName LIKE '%' || :query || '%' OR chapterName LIKE '%' || :query || '%' ORDER BY orderIndex ASC")
    fun searchLibraryFlow(query: String): Flow<List<IslamicLibraryEntity>>

    @Query("SELECT * FROM islamic_library_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): IslamicLibraryEntity?

    @Query("SELECT COUNT(*) FROM islamic_library_items")
    suspend fun getLibraryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<IslamicLibraryEntity>)
}

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY createdAt DESC")
    fun getAllWallpapersFlow(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE category = :category ORDER BY createdAt DESC")
    fun getWallpapersByCategoryFlow(category: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE titleArabic LIKE '%' || :query || '%' OR titleEnglish LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchWallpapersFlow(query: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritesFlow(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1 ORDER BY createdAt DESC")
    fun getDownloadedFlow(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: String): WallpaperEntity?

    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun getWallpapersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Update
    suspend fun updateWallpaper(wallpaper: WallpaperEntity)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET isDownloaded = :isDownloaded, localFilePath = :localFilePath WHERE id = :id")
    suspend fun setDownloaded(id: String, isDownloaded: Boolean, localFilePath: String?)

    @Query("UPDATE wallpapers SET isDownloaded = 0, localFilePath = NULL WHERE id = :id")
    suspend fun deleteLocalFile(id: String)

    @Query("DELETE FROM wallpapers")
    suspend fun clearAllWallpapers()
}

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getConfig(appWidgetId: Int): WidgetConfigEntity?

    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId LIMIT 1")
    fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfigEntity?>

    @Query("SELECT * FROM widget_configs")
    suspend fun getAllConfigs(): List<WidgetConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun deleteConfig(appWidgetId: Int)
}



