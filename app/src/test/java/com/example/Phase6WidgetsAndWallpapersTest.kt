package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.core.prayer.PrayerCalculator
import com.example.data.importer.WallpaperDataImporter
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.TasbeehStateEntity
import com.example.data.local.entity.WallpaperEntity
import com.example.data.local.entity.WidgetConfigEntity
import com.example.data.repository.WallpaperRepositoryImpl
import com.example.data.repository.WidgetConfigRepositoryImpl
import com.example.widgets.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase6WidgetsAndWallpapersTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.buildInMemory(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testWallpaperDaoAndRepository() = runTest {
        val wallpaperDao = database.wallpaperDao()
        val repo = WallpaperRepositoryImpl(wallpaperDao)

        val wp1 = WallpaperEntity(
            id = "wp_test_1",
            titleArabic = "المسجد النبوي الشريف",
            titleEnglish = "Prophet Mosque",
            category = "prophet_mosque",
            imageUrl = "https://images.unsplash.com/test1",
            thumbnailUrl = "https://images.unsplash.com/thumb1",
            source = "Unsplash",
            designerOrAuthor = "Sulthan",
            isFavorite = false
        )

        val wp2 = WallpaperEntity(
            id = "wp_test_2",
            titleArabic = "الكعبة المشرفة",
            titleEnglish = "Holy Kaaba",
            category = "kaaba",
            imageUrl = "https://images.unsplash.com/test2",
            thumbnailUrl = "https://images.unsplash.com/thumb2",
            source = "Unsplash",
            designerOrAuthor = "Haidar",
            isFavorite = true
        )

        wallpaperDao.insertWallpapers(listOf(wp1, wp2))

        val count = wallpaperDao.getWallpapersCount()
        assertEquals(2, count)

        // Filter by category
        val kaabaList = repo.getWallpapersByCategory("kaaba").first()
        assertEquals(1, kaabaList.size)
        assertEquals("الكعبة المشرفة", kaabaList[0].titleArabic)

        // Favorites
        val favorites = repo.getFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("wp_test_2", favorites[0].id)

        // Toggle favorite
        repo.toggleFavorite("wp_test_1", true)
        val updatedWp1 = repo.getWallpaperById("wp_test_1")
        assertNotNull(updatedWp1)
        assertTrue(updatedWp1!!.isFavorite)

        // Search
        val searchResult = repo.searchWallpapers("النبوي").first()
        assertEquals(1, searchResult.size)
        assertEquals("wp_test_1", searchResult[0].id)
    }

    @Test
    fun testWallpaperDataImporterSeeding() = runTest {
        val wallpaperDao = database.wallpaperDao()
        val importer = WallpaperDataImporter(context, wallpaperDao)

        importer.seedWallpapersIfNeeded()

        val count = wallpaperDao.getWallpapersCount()
        assertTrue("Expected at least 150 wallpapers from verified dataset, found: $count", count >= 150)

        // Verify categories exist
        val mosques = wallpaperDao.getWallpapersByCategoryFlow("mosques").first()
        assertTrue("Mosques category should have wallpapers", mosques.isNotEmpty())

        val calligraphy = wallpaperDao.getWallpapersByCategoryFlow("calligraphy").first()
        assertTrue("Calligraphy category should have wallpapers", calligraphy.isNotEmpty())

        val ramadan = wallpaperDao.getWallpapersByCategoryFlow("ramadan").first()
        assertTrue("Ramadan category should have wallpapers", ramadan.isNotEmpty())
    }

    @Test
    fun testWidgetConfigRepository() = runTest {
        val widgetConfigDao = database.widgetConfigDao()
        val repo = WidgetConfigRepositoryImpl(widgetConfigDao)

        val config = WidgetConfigEntity(
            appWidgetId = 101,
            widgetType = "prayer_daily",
            cityName = "المدينة المنورة",
            latitude = 24.4672,
            longitude = 39.6111,
            calculationMethod = "UMM_AL_QURA"
        )

        repo.saveConfig(config)

        val retrieved = repo.getConfig(101)
        assertNotNull(retrieved)
        assertEquals("المدينة المنورة", retrieved!!.cityName)
        assertEquals("UMM_AL_QURA", retrieved.calculationMethod)

        // Delete config
        repo.deleteConfig(101)
        val deleted = repo.getConfig(101)
        assertNull(deleted)
    }

    @Test
    fun testWidgetUpdateCoordinatorBuildsViews() = runTest {
        val todaySchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = Date()
        )
        val tomorrowSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = Date(System.currentTimeMillis() + 86400000L)
        )

        // 1. Prayer Next Widget
        val prayerNextViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 1,
            providerClass = PrayerNextWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(prayerNextViews)
        assertEquals(R.layout.widget_prayer_next, prayerNextViews.layoutId)

        // 2. Daily Prayer Times Widget
        val dailyViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 2,
            providerClass = PrayerTimesDailyWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(dailyViews)

        // 3. Quran Ayah Widget
        val ayahViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 3,
            providerClass = QuranAyahOfDayWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(ayahViews)

        // 4. Tasbeeh Counter Widget
        val tasbeehViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 4,
            providerClass = TasbeehCounterWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(tasbeehViews)

        // 5. Qibla Direction Widget
        val qiblaViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 5,
            providerClass = QiblaDirectionWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(qiblaViews)

        // 6. Summary Widget
        val summaryViews = WidgetUpdateCoordinator.buildViewsForProvider(
            context = context,
            appWidgetId = 6,
            providerClass = TodaySummaryWidgetProvider::class.java,
            todaySchedule = todaySchedule,
            tomorrowSchedule = tomorrowSchedule,
            lat = 21.4225,
            lon = 39.8262,
            cityName = "مكة المكرمة"
        )
        assertNotNull(summaryViews)
    }

    @Test
    fun testAll44WidgetProvidersAreRegisteredInCoordinator() {
        val providers = WidgetUpdateCoordinator.allProviderClasses
        assertTrue("Expected at least 35 widget providers, found: ${providers.size}", providers.size >= 35)
        assertEquals(44, providers.size)
    }

    @Test
    fun testTasbeehStateDatabasePersistence() = runTest {
        val tasbeehDao = database.dhikrDao()
        val initial = tasbeehDao.getTasbeehState()
        assertNull(initial)

        val newState = TasbeehStateEntity(
            id = 1,
            phrase = "سبحان الله العظيم",
            count = 15,
            target = 33
        )
        tasbeehDao.saveTasbeehState(newState)

        val loaded = tasbeehDao.getTasbeehState()
        assertNotNull(loaded)
        assertEquals(15, loaded!!.count)
        assertEquals("سبحان الله العظيم", loaded.phrase)

        // Increment count
        tasbeehDao.saveTasbeehState(loaded.copy(count = loaded.count + 1))
        val incremented = tasbeehDao.getTasbeehState()
        assertEquals(16, incremented!!.count)

        // Reset
        tasbeehDao.saveTasbeehState(incremented.copy(count = 0))
        val reset = tasbeehDao.getTasbeehState()
        assertEquals(0, reset!!.count)
    }
}
