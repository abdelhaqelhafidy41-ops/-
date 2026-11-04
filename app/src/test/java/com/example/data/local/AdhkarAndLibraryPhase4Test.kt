package com.example.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.importer.AdhkarDataImporter
import com.example.data.importer.QuranDataImporter
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.IslamicLibraryEntity
import com.example.data.local.entity.QuranSurahEntity
import com.example.data.local.entity.TasbeehStateEntity
import com.example.data.repository.DhikrRepositoryImpl
import com.example.data.repository.FavoritesRepositoryImpl
import com.example.data.repository.LibraryRepositoryImpl
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdhkarAndLibraryPhase4Test {

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
    fun testAdhkarImportAll12Categories() = runTest {
        val dhikrDao = database.dhikrDao()
        val libraryDao = database.libraryDao()
        val importer = AdhkarDataImporter(context, dhikrDao, libraryDao)

        importer.seedInitialDataIfNeeded()

        val count = dhikrDao.getDhikrCount()
        assertTrue("Expected at least 25 verified adhkar items, found $count", count >= 25)

        val allItems = dhikrDao.getAllDhikr().first()
        val categories = allItems.map { it.category }.distinct()

        val requiredCategories = listOf(
            "أذكار الصباح",
            "أذكار المساء",
            "أذكار بعد الصلاة",
            "أذكار النوم",
            "أذكار الاستيقاظ",
            "أذكار دخول وخروج المنزل",
            "أذكار المسجد",
            "أذكار السفر",
            "الرقية الشرعية",
            "أدعية القرآن",
            "أدعية الأنبياء",
            "أدعية متنوعة"
        )

        for (cat in requiredCategories) {
            assertTrue("Category '$cat' should be present in seeded adhkar", categories.contains(cat))
        }

        // Verify every item has Arabic text and reference
        for (item in allItems) {
            assertTrue("Arabic text cannot be empty", item.arabicText.isNotBlank())
            assertTrue("Reference must be present for item ${item.title}", !item.reference.isNullOrBlank())
            assertTrue("Grade must be present", !item.grade.isNullOrBlank())
        }
    }

    @Test
    fun testAdhkarSearchByArabicText() = runTest {
        val dhikrDao = database.dhikrDao()
        val libraryDao = database.libraryDao()
        val importer = AdhkarDataImporter(context, dhikrDao, libraryDao)
        importer.seedInitialDataIfNeeded()

        val results = dhikrDao.searchDhikr("الكرسي").first()
        assertTrue("Search for 'الكرسي' should find Ayat Al-Kursi", results.isNotEmpty())
        assertTrue(results.any { it.title.contains("الكرسي") })

        val istighfarResults = dhikrDao.searchDhikr("الاستغفار").first()
        assertTrue("Search for 'الاستغفار' should return relevant items", istighfarResults.isNotEmpty())
    }

    @Test
    fun testElectronicTasbeehPersistence() = runTest {
        val dhikrDao = database.dhikrDao()
        val repo = DhikrRepositoryImpl(dhikrDao)

        // Save initial state
        repo.saveTasbeehState(
            TasbeehStateEntity(
                id = 1,
                phrase = "سبحان الله وبحمده",
                count = 32,
                target = 33,
                vibrationEnabled = true
            )
        )

        // Read and verify
        val state = repo.getTasbeehState().firstOrNull()
        assertNotNull(state)
        assertEquals(32, state?.count)
        assertEquals("سبحان الله وبحمده", state?.phrase)
        assertEquals(33, state?.target)
        assertTrue(state?.vibrationEnabled == true)

        // Increment count and save
        repo.saveTasbeehState(state!!.copy(count = 33))
        val updated = repo.getTasbeehState().firstOrNull()
        assertEquals(33, updated?.count)

        // Reset
        repo.saveTasbeehState(updated!!.copy(count = 0))
        val resetState = repo.getTasbeehState().firstOrNull()
        assertEquals(0, resetState?.count)
    }

    @Test
    fun testIslamicLibraryImportAndAuthenticBooks() = runTest {
        val dhikrDao = database.dhikrDao()
        val libraryDao = database.libraryDao()
        val importer = AdhkarDataImporter(context, dhikrDao, libraryDao)
        importer.seedInitialDataIfNeeded()

        val count = libraryDao.getLibraryCount()
        assertTrue("Expected at least 15 verified library items, found $count", count >= 15)

        val bukhariItems = libraryDao.getItemsByBookFlow("BUKHARI").first()
        assertTrue("Sahih Al-Bukhari should contain items", bukhariItems.isNotEmpty())
        assertEquals("صحيح البخاري", bukhariItems.first().bookName)
        assertEquals("صحيح", bukhariItems.first().grade)

        val muslimItems = libraryDao.getItemsByBookFlow("MUSLIM").first()
        assertTrue("Sahih Muslim should contain items", muslimItems.isNotEmpty())

        val nawawiItems = libraryDao.getItemsByBookFlow("NAWAWI_40").first()
        assertTrue("Al-Arba'in An-Nawawiyyah should contain items", nawawiItems.isNotEmpty())

        val riyadItems = libraryDao.getItemsByBookFlow("RIYAD_SALIHIN").first()
        assertTrue("Riyad As-Salihin should contain items", riyadItems.isNotEmpty())

        val seerahItems = libraryDao.getItemsByBookFlow("SEERAH").first()
        assertTrue("Seerah Nabawiyyah should contain items", seerahItems.isNotEmpty())
    }

    @Test
    fun testLibrarySearchAcrossBooksAndHadiths() = runTest {
        val dhikrDao = database.dhikrDao()
        val libraryDao = database.libraryDao()
        val importer = AdhkarDataImporter(context, dhikrDao, libraryDao)
        importer.seedInitialDataIfNeeded()

        val results = libraryDao.searchLibraryFlow("النيات").first()
        assertTrue("Search for 'النيات' should return the Hadith of Intention", results.isNotEmpty())
        assertTrue(results.first().textArabic.contains("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"))

        val adviceResults = libraryDao.searchLibraryFlow("النصيحة").first()
        assertTrue("Search for 'النصيحة' should return Hadith", adviceResults.isNotEmpty())
    }

    @Test
    fun testBaqarahCompleteAyahsRequirement() = runTest {
        val quranDao = database.quranDao()
        val readingProgressDao = database.readingProgressDao()
        val importer = QuranDataImporter(context, quranDao)
        val quranRepo = QuranRepositoryImpl(quranDao, readingProgressDao, importer)

        // Insert Surah Al-Baqarah metadata
        quranDao.insertSurahs(
            listOf(
                QuranSurahEntity(
                    surahNumber = 2,
                    nameArabic = "البقرة",
                    nameEnglish = "Al-Baqarah",
                    revelationType = "مدنية",
                    totalAyahs = 286,
                    startPage = 2
                )
            )
        )

        // If only 6 ayahs exist (like the old bug), isSurahDownloaded must be false!
        val dummyAyahs = (1..6).map {
            com.example.data.local.entity.QuranAyahEntity(
                surahNumber = 2,
                ayahNumberInSurah = it,
                ayahNumberOverall = it + 7,
                textUthmani = "آية تجريبية رقم $it",
                pageNumber = 2,
                juzNumber = 1
            )
        }
        quranDao.insertAyahs(dummyAyahs)

        assertFalse(
            "Surah 2 has 286 ayahs. Having only 6 should NOT be considered downloaded!",
            quranRepo.isSurahDownloaded(2)
        )

        // Delete partial ayahs cleans up
        quranDao.deleteAyahsForSurah(2)
        assertEquals(0, quranDao.getAyahsCountForSurah(2))
    }

    @Test
    fun testFavoritesToggleForAdhkarAndHadith() = runTest {
        val favDao = database.favoriteDao()
        val favRepo = FavoritesRepositoryImpl(favDao)

        // Favorite Dhikr
        val isAdded = favRepo.toggleFavorite(
            itemType = "DHIKR",
            itemId = "101",
            title = "آية الكرسي",
            subtitle = "أذكار الصباح",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            reference = "سورة البقرة: 255"
        )
        assertTrue(isAdded)
        assertTrue(favRepo.isFavorite("DHIKR", "101").first())

        // Toggle removes favorite
        val isRemoved = favRepo.toggleFavorite(
            itemType = "DHIKR",
            itemId = "101",
            title = "آية الكرسي"
        )
        assertFalse(isRemoved)
        assertFalse(favRepo.isFavorite("DHIKR", "101").first())
    }
}
