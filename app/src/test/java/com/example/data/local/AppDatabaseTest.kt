package com.example.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.DhikrHistoryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PrayerTimeEntity
import com.example.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.buildInMemory(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testFavoriteDaoOperations() = runTest {
        val favoriteDao = database.favoriteDao()

        val item = FavoriteEntity(
            itemType = "QURAN_AYAH",
            itemId = "2:255",
            title = "آية الكرسي",
            subtitle = "سورة البقرة",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            reference = "2:255"
        )

        favoriteDao.insertFavorite(item)

        val isFav = favoriteDao.isFavoriteFlow("QURAN_AYAH", "2:255").first()
        assertTrue("Item should be bookmarked as favorite", isFav)

        val allFavorites = favoriteDao.getAllFavorites().first()
        assertEquals(1, allFavorites.size)
        assertEquals("آية الكرسي", allFavorites[0].title)

        favoriteDao.deleteFavorite("QURAN_AYAH", "2:255")
        val isFavAfterDelete = favoriteDao.isFavoriteFlow("QURAN_AYAH", "2:255").first()
        assertTrue("Item should no longer be marked as favorite", !isFavAfterDelete)
    }

    @Test
    fun testReadingProgressDaoOperations() = runTest {
        val readingProgressDao = database.readingProgressDao()

        val progress = ReadingProgressEntity(
            id = 1,
            surahNumber = 18,
            surahNameArabic = "الكهف",
            ayahNumber = 10,
            pageNumber = 294,
            juzNumber = 15
        )

        readingProgressDao.saveReadingProgress(progress)

        val retrieved = readingProgressDao.getReadingProgress()
        assertNotNull(retrieved)
        assertEquals(18, retrieved?.surahNumber)
        assertEquals("الكهف", retrieved?.surahNameArabic)
        assertEquals(10, retrieved?.ayahNumber)

        readingProgressDao.clearReadingProgress()
        val cleared = readingProgressDao.getReadingProgress()
        assertNull(cleared)
    }

    @Test
    fun testDhikrHistoryLogging() = runTest {
        val dhikrDao = database.dhikrDao()

        val session = DhikrHistoryEntity(
            dhikrTitle = "سبحان الله",
            countDone = 33,
            targetCount = 33,
            dateString = "2026-11-04"
        )

        dhikrDao.logDhikrSession(session)

        val total = dhikrDao.getTodayTotalDhikrCountFlow("2026-11-04").firstOrNull()
        assertEquals(33, total)

        val secondSession = DhikrHistoryEntity(
            dhikrTitle = "الحمد لله",
            countDone = 33,
            targetCount = 33,
            dateString = "2026-11-04"
        )
        dhikrDao.logDhikrSession(secondSession)

        val updatedTotal = dhikrDao.getTodayTotalDhikrCountFlow("2026-11-04").firstOrNull()
        assertEquals(66, updatedTotal)
    }

    @Test
    fun testPrayerTimesCaching() = runTest {
        val prayerTimeDao = database.prayerTimeDao()

        val prayerTime = PrayerTimeEntity(
            dateString = "2026-11-04",
            cityName = "مكة المكرمة",
            latitude = 21.4225,
            longitude = 39.8262,
            fajrTime = "05:08",
            sunriseTime = "06:25",
            dhuhrTime = "12:10",
            asrTime = "15:25",
            maghribTime = "17:50",
            ishaTime = "19:20",
            calculationMethod = "UMM_AL_QURA"
        )

        prayerTimeDao.insertPrayerTime(prayerTime)

        val retrieved = prayerTimeDao.getPrayerTimeFlow("2026-11-04", "مكة المكرمة").first()
        assertNotNull(retrieved)
        assertEquals("05:08", retrieved?.fajrTime)
        assertEquals("UMM_AL_QURA", retrieved?.calculationMethod)
    }

    @Test
    fun testDailyAchievementDao() = runTest {
        val achievementDao = database.dailyAchievementDao()

        val achievement = DailyAchievementEntity(
            dateString = "2026-11-04",
            prayersCompletedMask = 31, // All 5 prayers (11111 binary)
            quranMinutesRead = 20,
            quranAyahsRead = 35,
            totalDhikrCount = 100,
            morningDhikrCompleted = true,
            eveningDhikrCompleted = true
        )

        achievementDao.saveDailyAchievement(achievement)

        val retrieved = achievementDao.getAchievementForDate("2026-11-04")
        assertNotNull(retrieved)
        assertEquals(31, retrieved?.prayersCompletedMask)
        assertEquals(20, retrieved?.quranMinutesRead)
        assertTrue(retrieved?.morningDhikrCompleted == true)
    }
}
