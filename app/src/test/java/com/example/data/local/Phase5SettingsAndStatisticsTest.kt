package com.example.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.KhatmahEntity
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.preferences.FontFamilyType
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferencesDataStore
import com.example.data.repository.AchievementRepository
import com.example.data.repository.AchievementRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase5SettingsAndStatisticsTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var database: AppDatabase
    private lateinit var dataStore: UserPreferencesDataStore
    private lateinit var achievementRepository: AchievementRepository

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        database = AppDatabase.buildInMemory(context)

        val testPreferencesDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_phase5_prefs_${System.nanoTime()}.preferences_pb") }
        )
        dataStore = UserPreferencesDataStore(testPreferencesDataStore)

        achievementRepository = AchievementRepositoryImpl(
            dailyAchievementDao = database.dailyAchievementDao(),
            dhikrDao = database.dhikrDao(),
            favoriteDao = database.favoriteDao(),
            readingProgressDao = database.readingProgressDao(),
            khatmahDao = database.khatmahDao(),
            memorizationDao = database.memorizationDao(),
            userPreferencesDataStore = dataStore
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPhase5PreferencesAndResetToDefaults() = runTest {
        dataStore.updateFontFamilyType(FontFamilyType.AMIRI)
        dataStore.updatePreferredReciter("Husary_128kbps")
        dataStore.updateVibrationEnabled(false)
        dataStore.updateDailyDhikrReminderEnabled(false)

        var prefs = dataStore.userPreferencesFlow.first()
        assertEquals(FontFamilyType.AMIRI, prefs.fontFamilyType)
        assertEquals("Husary_128kbps", prefs.preferredReciter)
        assertFalse(prefs.vibrationEnabled)
        assertFalse(prefs.dailyDhikrReminderEnabled)

        // Reset to defaults
        dataStore.resetToDefaults()
        prefs = dataStore.userPreferencesFlow.first()
        assertEquals(FontFamilyType.CAIRO, prefs.fontFamilyType)
        assertEquals("Alafasy_128kbps", prefs.preferredReciter)
        assertTrue(prefs.vibrationEnabled)
        assertTrue(prefs.dailyDhikrReminderEnabled)
    }

    @Test
    fun testPrayerToggleBitmask() = runTest {
        // Toggle Fajr (prayerIndex = 0)
        achievementRepository.togglePrayerCompleted(0)
        var today = achievementRepository.getTodayAchievementSync()
        assertNotNull(today)
        assertEquals(1, today!!.prayersCompletedMask and 1)
        assertEquals(1, Integer.bitCount(today.prayersCompletedMask))

        // Toggle Dhuhr (prayerIndex = 1)
        achievementRepository.togglePrayerCompleted(1)
        today = achievementRepository.getTodayAchievementSync()
        assertEquals(3, today!!.prayersCompletedMask) // 1 or 2 = 3
        assertEquals(2, Integer.bitCount(today.prayersCompletedMask))

        // Toggle Fajr again (should turn it off)
        achievementRepository.togglePrayerCompleted(0)
        today = achievementRepository.getTodayAchievementSync()
        assertEquals(2, today!!.prayersCompletedMask)
        assertEquals(1, Integer.bitCount(today.prayersCompletedMask))
    }

    @Test
    fun testAccumulateReadingAndDhikr() = runTest {
        achievementRepository.logReadingProgress(ayahsRead = 15, minutesRead = 10)
        achievementRepository.logReadingProgress(ayahsRead = 10, minutesRead = 5)
        achievementRepository.logDhikrCount(33)
        achievementRepository.logDhikrCount(33)

        val today = achievementRepository.getTodayAchievementSync()
        assertNotNull(today)
        assertEquals(25, today!!.quranAyahsRead)
        assertEquals(15, today.quranMinutesRead)
        assertEquals(66, today.totalDhikrCount)
    }

    @Test
    fun testStreakCalculation() = runTest {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        // Initially no activity -> streak = 0
        assertEquals(0, achievementRepository.calculateStreakDays())

        // Day 0 (today): 5 prayers
        val todayStr = fmt.format(cal.time)
        database.dailyAchievementDao().saveDailyAchievement(
            DailyAchievementEntity(
                dateString = todayStr,
                prayersCompletedMask = 31, // all 5 prayers
                quranMinutesRead = 20,
                quranAyahsRead = 30,
                totalDhikrCount = 100,
                morningDhikrCompleted = true,
                eveningDhikrCompleted = true
            )
        )

        // Day -1 (yesterday): dhikr activity
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = fmt.format(cal.time)
        database.dailyAchievementDao().saveDailyAchievement(
            DailyAchievementEntity(
                dateString = yesterdayStr,
                prayersCompletedMask = 0,
                quranMinutesRead = 0,
                quranAyahsRead = 0,
                totalDhikrCount = 33,
                morningDhikrCompleted = true,
                eveningDhikrCompleted = false
            )
        )

        // Day -2 (two days ago): quran activity
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgoStr = fmt.format(cal.time)
        database.dailyAchievementDao().saveDailyAchievement(
            DailyAchievementEntity(
                dateString = twoDaysAgoStr,
                prayersCompletedMask = 1,
                quranMinutesRead = 10,
                quranAyahsRead = 5,
                totalDhikrCount = 0,
                morningDhikrCompleted = false,
                eveningDhikrCompleted = false
            )
        )

        // Now streak should be exactly 3 days
        val streak = achievementRepository.calculateStreakDays()
        assertEquals(3, streak)
    }

    @Test
    fun testExportDataJsonAndCsv() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        database.dailyAchievementDao().saveDailyAchievement(
            DailyAchievementEntity(
                dateString = today,
                prayersCompletedMask = 31,
                quranMinutesRead = 15,
                quranAyahsRead = 20,
                totalDhikrCount = 100,
                morningDhikrCompleted = true,
                eveningDhikrCompleted = true
            )
        )

        val json = achievementRepository.exportUserDataJson()
        assertTrue(json.contains("\"app\": \"نور المسلم\""))
        assertTrue(json.contains(today))
        assertTrue(json.contains("\"prayersMask\": 31"))

        val csv = achievementRepository.exportUserDataCsv()
        assertTrue(csv.contains("Date,PrayersCompletedCount"))
        assertTrue(csv.contains(today))
        assertTrue(csv.contains(",5,31,20,15,100,true,true"))
    }

    @Test
    fun testClearAllUserDataLeavesContentIntact() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        database.dailyAchievementDao().saveDailyAchievement(
            DailyAchievementEntity(
                dateString = today,
                prayersCompletedMask = 15,
                quranMinutesRead = 10,
                quranAyahsRead = 10,
                totalDhikrCount = 33,
                morningDhikrCompleted = false,
                eveningDhikrCompleted = false
            )
        )
        database.favoriteDao().insertFavorite(
            FavoriteEntity(
                itemType = "quran",
                itemId = "1:1",
                title = "الفاتحة",
                subtitle = "آية 1",
                createdAt = System.currentTimeMillis()
            )
        )
        database.khatmahDao().insertKhatmah(
            KhatmahEntity(
                title = "ختمة رمضان",
                targetDays = 30,
                startDate = today,
                targetEndDate = today,
                targetType = "page",
                currentProgress = 10,
                totalTarget = 604,
                isCompleted = false,
                isPaused = false,
                createdAt = System.currentTimeMillis()
            )
        )

        assertEquals(1, database.dailyAchievementDao().getAllAchievementsList().size)
        assertEquals(1, database.favoriteDao().getAllFavoritesList().size)
        assertEquals(1, database.khatmahDao().getAllKhatmahsList().size)

        // Clear all user data
        achievementRepository.clearAllUserData()

        assertEquals(0, database.dailyAchievementDao().getAllAchievementsList().size)
        assertEquals(0, database.favoriteDao().getAllFavoritesList().size)
        assertEquals(0, database.khatmahDao().getAllKhatmahsList().size)
        assertEquals(0, database.dhikrDao().getAllDhikrHistoryList().size)
        assertEquals(0, database.memorizationDao().getAllMemorizationRecordsList().size)
    }
}
