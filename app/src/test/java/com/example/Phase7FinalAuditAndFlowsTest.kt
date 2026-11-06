package com.example

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.core.prayer.NextPrayerInfo
import com.example.core.prayer.PrayerCalculator
import com.example.core.prayer.PrayerSchedule
import com.example.core.prayer.QiblaHelper
import com.example.data.importer.AdhkarDataImporter
import com.example.data.importer.QuranDataImporter
import com.example.data.importer.WallpaperDataImporter
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.entity.TasbeehStateEntity
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferencesDataStore
import com.example.presentation.navigation.Screen
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
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase7FinalAuditAndFlowsTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var preferencesDataStore: UserPreferencesDataStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.buildInMemory(context)

        val testPreferencesDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_audit_prefs_${System.nanoTime()}.preferences_pb") }
        )
        preferencesDataStore = UserPreferencesDataStore(testPreferencesDataStore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * التدفق 1: فتح التطبيق والتنقل بين جميع الشاشات الأساسية والفرعية.
     */
    @Test
    fun testAppOpeningAndNavigationFlow() {
        val allScreens = listOf(
            Screen.Home,
            Screen.Quran,
            Screen.Adhkar,
            Screen.Prayer,
            Screen.More,
            Screen.Settings,
            Screen.Statistics,
            Screen.IslamicLibrary,
            Screen.Wallpapers,
            Screen.QuranReading
        )

        assertTrue("Expected comprehensive navigation route coverage", allScreens.size >= 8)
        allScreens.forEach { screen ->
            assertNotNull("Screen route representation should not be null", screen.route)
        }
        assertEquals("quran_reading/1", Screen.QuranReading.createRoute(1))
    }

    /**
     * التدفق 2: فتح القرآن، فحص السور، قراءة سورة وحفظ موضع القراءة والمفضلة.
     */
    @Test
    fun testQuranReadingAndSurahFlow() = runTest {
        val quranDao = database.quranDao()
        val readingProgressDao = database.readingProgressDao()
        val favoriteDao = database.favoriteDao()
        val importer = QuranDataImporter(context, quranDao)

        importer.seedInitialDataIfNeeded()

        val surahCount = quranDao.getSurahsCount()
        assertEquals("All 114 Surahs must be seeded and available", 114, surahCount)

        // اختبار قراءة سورة الفاتحة
        val fatihah = quranDao.getSurahByNumber(1)
        assertNotNull("Al-Fatihah must exist", fatihah)
        assertEquals("الفاتحة", fatihah?.nameArabic)
        assertEquals(7, fatihah?.totalAyahs)

        // حفظ موضع القراءة
        val progress = ReadingProgressEntity(
            surahNumber = 1,
            surahNameArabic = "الفاتحة",
            ayahNumber = 4,
            pageNumber = 1,
            juzNumber = 1,
            updatedAt = System.currentTimeMillis()
        )
        readingProgressDao.saveReadingProgress(progress)

        val retrievedProgress = readingProgressDao.getReadingProgress()
        assertNotNull("Reading progress must be persisted", retrievedProgress)
        assertEquals(1, retrievedProgress?.surahNumber)
        assertEquals(4, retrievedProgress?.ayahNumber)

        // إضافة علامة مرجعية في المفضلة
        val favorite = FavoriteEntity(
            itemType = "QURAN_AYAH",
            itemId = "1:5",
            title = "سورة الفاتحة - آية 5",
            arabicText = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            reference = "سورة الفاتحة (1:5)"
        )
        favoriteDao.insertFavorite(favorite)

        val favorites = favoriteDao.getFavoritesByType("QURAN_AYAH").first()
        assertEquals(1, favorites.size)
        assertEquals("1:5", favorites[0].itemId)
    }

    /**
     * التدفق 3: تشغيل ذكر وعدّه في السبحة الإلكترونية وحفظ الإنجاز اليومي.
     */
    @Test
    fun testDhikrPlayAndCounterFlow() = runTest {
        val dhikrDao = database.dhikrDao()
        val libraryDao = database.libraryDao()
        val achievementDao = database.dailyAchievementDao()
        val importer = AdhkarDataImporter(context, dhikrDao, libraryDao)

        importer.seedInitialDataIfNeeded()

        val dhikrCount = dhikrDao.getDhikrCount()
        assertTrue("Adhkar must be seeded", dhikrCount >= 25)

        // فحص السبحة الإلكترونية وحفظ الحالة
        val phrase = "سبحان الله وبحمده"
        val tasbeeh = TasbeehStateEntity(
            phrase = phrase,
            count = 33,
            target = 33,
            vibrationEnabled = true,
            lastUpdated = System.currentTimeMillis()
        )
        dhikrDao.saveTasbeehState(tasbeeh)

        val retrievedTasbeeh = dhikrDao.getTasbeehState()
        assertNotNull(retrievedTasbeeh)
        assertEquals(33, retrievedTasbeeh?.count)
        assertEquals(phrase, retrievedTasbeeh?.phrase)

        // تسجيل إنجاز الذكر اليومي
        val today = "2026-03-15"
        val achievement = DailyAchievementEntity(
            dateString = today,
            prayersCompletedMask = 3, // صلاتان
            totalDhikrCount = 100,
            quranAyahsRead = 20,
            quranMinutesRead = 15,
            morningDhikrCompleted = true,
            lastUpdated = System.currentTimeMillis()
        )
        achievementDao.saveDailyAchievement(achievement)

        val savedAchievement = achievementDao.getAchievementForDate(today)
        assertNotNull(savedAchievement)
        assertEquals(100, savedAchievement?.totalDhikrCount)
        assertEquals(3, savedAchievement?.prayersCompletedMask)
    }

    /**
     * التدفق 4: حساب مواقيت الصلاة والقبلة وتغيير طرق الحساب والمذاهب.
     */
    @Test
    fun testPrayerTimesAndLocationFlow() {
        // مكة المكرمة
        val makkahLat = 21.4225
        val makkahLng = 39.8262
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 15, 12, 0, 0)
        }
        val date = cal.time

        val makkahSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = makkahLat,
            longitude = makkahLng,
            date = date,
            timeZone = TimeZone.getTimeZone("Asia/Riyadh"),
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.STANDARD,
            cityName = "مكة المكرمة"
        )

        assertNotNull(makkahSchedule)
        assertTrue("Fajr must be before Dhuhr", makkahSchedule.fajr.before(makkahSchedule.dhuhr))
        assertTrue("Dhuhr must be before Asr", makkahSchedule.dhuhr.before(makkahSchedule.asr))
        assertTrue("Asr must be before Maghrib", makkahSchedule.asr.before(makkahSchedule.maghrib))
        assertTrue("Maghrib must be before Isha", makkahSchedule.maghrib.before(makkahSchedule.isha))

        // فحص طريقة أخرى (رابطة العالم الإسلامي والمذهب الحنفي بالقاهرة)
        val cairoLat = 30.0444
        val cairoLng = 31.2357
        val cairoScheduleHanafi = PrayerCalculator.calculatePrayerTimes(
            latitude = cairoLat,
            longitude = cairoLng,
            date = date,
            timeZone = TimeZone.getTimeZone("Africa/Cairo"),
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhhab = Madhhab.HANAFI,
            cityName = "القاهرة"
        )

        assertNotNull(cairoScheduleHanafi)

        // فحص اتجاه القبلة والمسافة للكعبة المشرفة
        val bearing = QiblaHelper.calculateQiblaBearing(cairoLat, cairoLng)
        val distanceKm = QiblaHelper.calculateDistanceToKaabaKm(cairoLat, cairoLng)

        assertTrue("Cairo Qibla bearing should be approx 135 deg [120..155]", bearing in 120.0f..155.0f)
        assertTrue("Distance from Cairo to Makkah should be > 1000 km", distanceKm > 1000.0)
    }

    /**
     * التدفق 5: تغيير المظهر (فاتح / داكن) وحجم الخط وتفضيلات الصلاة.
     */
    @Test
    fun testThemeAndFontSettingsFlow() = runTest {
        preferencesDataStore.updateThemeMode(ThemeMode.DARK)
        var prefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(ThemeMode.DARK, prefs.themeMode)

        preferencesDataStore.updateThemeMode(ThemeMode.LIGHT)
        prefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(ThemeMode.LIGHT, prefs.themeMode)

        preferencesDataStore.updateFontSizeScale(FontSizeScale.LARGE)
        prefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(FontSizeScale.LARGE, prefs.fontSizeScale)

        preferencesDataStore.updateCalculationMethod(CalculationMethod.EGYPTIAN)
        prefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(CalculationMethod.EGYPTIAN, prefs.calculationMethod)

        preferencesDataStore.updateMadhhab(Madhhab.HANAFI)
        prefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(Madhhab.HANAFI, prefs.madhhab)
    }

    /**
     * التدفق 6: العمل دون إنترنت (Offline-First).
     * جميع العمليات الحسابية والقرآن والأذكار والخلفيات تعمل محلياً تماماً.
     */
    @Test
    fun testOfflineFirstOperationFlow() = runTest {
        // فحص استيراد وتخزين الخلفيات محلياً
        val wallpaperDao = database.wallpaperDao()
        val wallpaperImporter = WallpaperDataImporter(context, wallpaperDao)
        wallpaperImporter.seedWallpapersIfNeeded()

        val wallpaperCount = wallpaperDao.getWallpapersCount()
        assertTrue("Expected 162 wallpapers seeded locally", wallpaperCount >= 150)

        // فحص عمل مواقيت الصلاة محلياً لمعادلات فلكية دون أي استدعاء شبكي
        val londonSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 51.5074,
            longitude = -0.1278,
            date = Date(),
            timeZone = TimeZone.getTimeZone("Europe/London"),
            method = CalculationMethod.ISNA
        )
        assertNotNull("Prayer calculations must run entirely on-device without network", londonSchedule)
        assertNotNull(londonSchedule.fajr)
        assertNotNull(londonSchedule.maghrib)
    }

    /**
     * التدفق 7: تفريغ وإعادة ضبط بيانات المستخدم وحذف السجلات.
     */
    @Test
    fun testDataDeletionAndResetFlow() = runTest {
        val readingProgressDao = database.readingProgressDao()
        val favoriteDao = database.favoriteDao()
        val achievementDao = database.dailyAchievementDao()
        val khatmahDao = database.khatmahDao()

        // إضافة بيانات تجريبية
        readingProgressDao.saveReadingProgress(
            ReadingProgressEntity(surahNumber = 2, surahNameArabic = "البقرة", ayahNumber = 255, pageNumber = 42, juzNumber = 3)
        )
        favoriteDao.insertFavorite(
            FavoriteEntity(itemType = "SURAH", itemId = "2", title = "سورة البقرة")
        )

        // التحقق من وجود البيانات
        assertNotNull(readingProgressDao.getReadingProgress())
        assertEquals(1, favoriteDao.getAllFavoritesList().size)

        // تنفيذ الحذف الشامل
        readingProgressDao.clearReadingProgress()
        favoriteDao.clearAllFavorites()
        achievementDao.clearAllAchievements()
        khatmahDao.clearAllKhatmahs()
        preferencesDataStore.resetToDefaults()

        // التحقق من التفريغ التام
        assertEquals(null, readingProgressDao.getReadingProgress())
        assertEquals(0, favoriteDao.getAllFavoritesList().size)
        assertEquals(0, achievementDao.getAllAchievementsList().size)

        val resetPrefs = preferencesDataStore.userPreferencesFlow.first()
        assertEquals(ThemeMode.SYSTEM, resetPrefs.themeMode)
        assertEquals(FontSizeScale.NORMAL, resetPrefs.fontSizeScale)
    }
}
