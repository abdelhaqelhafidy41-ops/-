package com.example.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioPlayerState
import com.example.audio.QuranAudioPlayer
import com.example.audio.Reciter
import com.example.audio.RepeatMode
import com.example.data.importer.QuranDataImporter
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.KhatmahEntity
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.entity.QuranAyahEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.repository.FavoritesRepositoryImpl
import com.example.data.repository.KhatmahRepositoryImpl
import com.example.data.repository.QuranRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranPhase3Test {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

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
    fun test114SurahsImportAndCount() = runTest {
        val quranDao = database.quranDao()
        val readingProgressDao = database.readingProgressDao()
        val importer = QuranDataImporter(context, quranDao)

        // Seed initial data from verified JSON assets
        importer.seedInitialDataIfNeeded()

        val count = quranDao.getSurahsCount()
        assertEquals("There must be exactly 114 Surahs imported from verified catalog", 114, count)

        // Test first and last Surah
        val fatihah = quranDao.getSurahByNumber(1)
        assertNotNull(fatihah)
        assertEquals("الفاتحة", fatihah?.nameArabic)
        assertEquals(7, fatihah?.totalAyahs)
        assertEquals("مكية", fatihah?.revelationType)

        val nas = quranDao.getSurahByNumber(114)
        assertNotNull(nas)
        assertEquals("الناس", nas?.nameArabic)
        assertEquals(6, nas?.totalAyahs)
        assertEquals("مكية", nas?.revelationType)

        val baqarah = quranDao.getSurahByNumber(2)
        assertEquals(286, baqarah?.totalAyahs)
        assertEquals("مدنية", baqarah?.revelationType)
    }

    @Test
    fun testSurahAndAyahSearch() = runTest {
        val quranDao = database.quranDao()
        val importer = QuranDataImporter(context, quranDao)
        importer.seedInitialDataIfNeeded()

        // 1. Search Surah by Arabic name
        val searchByName = quranDao.searchSurahsFlow("الكهف").first()
        assertEquals(1, searchByName.size)
        assertEquals(18, searchByName[0].surahNumber)

        // 2. Search Surah by Number
        val searchByNum = quranDao.searchSurahsFlow("36").first()
        assertEquals(1, searchByNum.size)
        assertEquals("يس", searchByNum[0].nameArabic)

        // 3. Search Ayahs for text
        val ayahsSearch = quranDao.searchAyahsFlow("الرَّحْمَٰنِ").first()
        assertTrue("Search query must return authentic matching verses", ayahsSearch.isNotEmpty())
        assertTrue(ayahsSearch.any { it.textUthmani.contains("الرَّحْمَٰنِ") })
    }

    @Test
    fun testReadingProgressSaveAndRetrieve() = runTest {
        val quranDao = database.quranDao()
        val readingProgressDao = database.readingProgressDao()
        val importer = QuranDataImporter(context, quranDao)
        val repository = QuranRepositoryImpl(quranDao, readingProgressDao, importer)

        repository.saveReadingProgress(
            surahNumber = 18,
            surahName = "الكهف",
            ayahNumber = 10,
            page = 294,
            juz = 15
        )

        val progress = repository.getReadingProgress().first()
        assertNotNull(progress)
        assertEquals(18, progress?.surahNumber)
        assertEquals("الكهف", progress?.surahNameArabic)
        assertEquals(10, progress?.ayahNumber)
        assertEquals(294, progress?.pageNumber)

        repository.clearReadingProgress()
        val cleared = repository.getReadingProgress().first()
        assertEquals(null, cleared)
    }

    @Test
    fun testFavoritesAndBookmarks() = runTest {
        val favoriteDao = database.favoriteDao()
        val favRepo = FavoritesRepositoryImpl(favoriteDao)

        val isFavInitial = favRepo.isFavorite("QURAN_AYAH", "1:1").first()
        assertFalse(isFavInitial)

        // Toggle on
        val added = favRepo.toggleFavorite(
            itemType = "QURAN_AYAH",
            itemId = "1:1",
            title = "سورة الفاتحة - آية 1",
            subtitle = "الصفحة 1",
            arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            reference = "1:1"
        )
        assertTrue(added)

        val isFavAfter = favRepo.isFavorite("QURAN_AYAH", "1:1").first()
        assertTrue(isFavAfter)

        // Toggle off
        val removed = favRepo.toggleFavorite(
            itemType = "QURAN_AYAH",
            itemId = "1:1",
            title = "سورة الفاتحة - آية 1"
        )
        assertFalse(removed)

        val isFavFinal = favRepo.isFavorite("QURAN_AYAH", "1:1").first()
        assertFalse(isFavFinal)
    }

    @Test
    fun testKhatmahCalculationsAndProgress() = runTest {
        val khatmahDao = database.khatmahDao()
        val khatmahRepo = KhatmahRepositoryImpl(khatmahDao)

        // Create 30 days Khatmah (604 pages)
        val id = khatmahRepo.createKhatmah(
            title = "ختمة رمضان المبارك",
            targetDays = 30,
            targetType = "PAGES",
            totalTarget = 604
        )

        val created = khatmahRepo.getKhatmahById(id)
        assertNotNull(created)
        assertEquals("ختمة رمضان المبارك", created?.title)
        assertEquals(604, created?.totalTarget)
        assertEquals(0, created?.currentProgress)
        assertFalse(created?.isCompleted ?: true)

        // Daily portion: ceil(604 / 30) = 21 pages/day
        assertEquals(21, created?.dailyPortion)

        // Record progress: +151 pages (25% of 604)
        khatmahRepo.recordProgress(id, 151)
        val afterProgress = khatmahRepo.getKhatmahById(id)
        assertEquals(151, afterProgress?.currentProgress)
        assertEquals(25f, afterProgress?.progressPercentage ?: 0f, 0.5f)
        assertFalse(afterProgress?.isCompleted ?: true)

        // Set to full 604
        khatmahRepo.setKhatmahProgress(id, 604)
        val completed = khatmahRepo.getKhatmahById(id)
        assertEquals(604, completed?.currentProgress)
        assertTrue(completed?.isCompleted == true)
        assertEquals(100f, completed?.progressPercentage ?: 0f, 0.1f)
    }

    @Test
    fun testAudioPlayerStateAndReciters() {
        // EveryAyah URL generation check
        val alafasy = Reciter.AVAILABLE_RECITERS.first { it.id == "alafasy" }
        val url = alafasy.getAyahAudioUrl(surahNumber = 1, ayahNumber = 1)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/001001.mp3", url)

        val husary = Reciter.AVAILABLE_RECITERS.first { it.id == "husary" }
        val husaryUrl = husary.getAyahAudioUrl(surahNumber = 18, ayahNumber = 10)
        assertEquals("https://everyayah.com/data/Husary_128kbps/018010.mp3", husaryUrl)

        val player = QuranAudioPlayer(context)
        assertEquals(Reciter.DEFAULT.id, player.state.value.reciter.id)

        player.setReciter(husary)
        assertEquals("husary", player.state.value.reciter.id)

        player.setRepeatMode(RepeatMode.REPEAT_AYAH)
        assertEquals(RepeatMode.REPEAT_AYAH, player.state.value.repeatMode)

        player.stop()
        assertFalse(player.state.value.isPlaying)
    }

    @Test
    fun testOfflineOperationAfterDownload() = runTest {
        val quranDao = database.quranDao()
        val importer = QuranDataImporter(context, quranDao)
        importer.seedInitialDataIfNeeded()

        // Verify ayahs in database can be read without internet
        val fatihahAyahs = quranDao.getAyahsForSurah(1)
        assertTrue("Seed ayahs for Al-Fatihah must be available offline", fatihahAyahs.isNotEmpty())
        assertEquals(7, fatihahAyahs.size)
        assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", fatihahAyahs[0].textUthmani)

        // Check local audio caching detection
        val player = QuranAudioPlayer(context)
        val reciter = Reciter.DEFAULT
        val cached = player.isAyahCached(1, 1, reciter)
        // Verify method executes without error (returns boolean based on disk file)
        assertFalse("Un-downloaded audio should report not cached", cached)
    }
}
