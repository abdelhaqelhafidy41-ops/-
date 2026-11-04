package com.example.data.importer

import android.content.Context
import com.example.data.local.dao.QuranDao
import com.example.data.local.entity.QuranAyahEntity
import com.example.data.local.entity.QuranSurahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class QuranDataImporter(
    private val context: Context,
    private val quranDao: QuranDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Seeds initial 114 surahs and seed ayahs from verified local assets into Room
     */
    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val surahsCount = quranDao.getSurahsCount()
        if (surahsCount < 114) {
            importSurahsFromAssets()
        }

        val ayahsCount = quranDao.getAyahsCount()
        if (ayahsCount == 0) {
            importSeedAyahsFromAssets()
        }
    }

    private suspend fun importSurahsFromAssets() {
        try {
            val jsonString = context.assets.open("quran/surahs.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val surahs = mutableListOf<QuranSurahEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                surahs.add(
                    QuranSurahEntity(
                        surahNumber = obj.getInt("surahNumber"),
                        nameArabic = obj.getString("nameArabic"),
                        nameEnglish = obj.getString("nameEnglish"),
                        revelationType = if (obj.getString("revelationType") == "Meccan") "مكية" else "مدنية",
                        totalAyahs = obj.getInt("totalAyahs"),
                        startPage = obj.getInt("startPage")
                    )
                )
            }
            if (surahs.isNotEmpty()) {
                quranDao.insertSurahs(surahs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun importSeedAyahsFromAssets() {
        try {
            val jsonString = context.assets.open("quran/verified_ayahs_seed.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val ayahs = mutableListOf<QuranAyahEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                ayahs.add(
                    QuranAyahEntity(
                        surahNumber = obj.getInt("surahNumber"),
                        ayahNumberInSurah = obj.getInt("ayahNumberInSurah"),
                        ayahNumberOverall = obj.getInt("ayahNumberOverall"),
                        textUthmani = obj.getString("textUthmani"),
                        pageNumber = obj.getInt("pageNumber"),
                        juzNumber = obj.getInt("juzNumber"),
                        sajda = obj.optBoolean("sajda", false)
                    )
                )
            }
            if (ayahs.isNotEmpty()) {
                quranDao.insertAyahs(ayahs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Downloads authentic verses for a surah from Quran.com verified API v4 and saves to Room
     */
    suspend fun downloadSurahAyahs(surahNumber: Int): List<QuranAyahEntity> = withContext(Dispatchers.IO) {
        val surah = quranDao.getSurahByNumber(surahNumber)
            ?: throw IllegalArgumentException("سورة غير موجودة برقم: $surahNumber")

        val existingCount = quranDao.getAyahsCountForSurah(surahNumber)
        if (existingCount >= surah.totalAyahs) {
            return@withContext quranDao.getAyahsForSurah(surahNumber)
        }

        // Clear any previous partial / incomplete verses for this surah
        quranDao.deleteAyahsForSurah(surahNumber)

        val ayahsList = mutableListOf<QuranAyahEntity>()
        var page = 1
        var hasMore = true

        try {
            while (hasMore && ayahsList.size < surah.totalAyahs) {
                val url = "https://api.quran.com/api/v4/quran/verses/uthmani?chapter_number=$surahNumber&page=$page&per_page=100"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("فشل الاتصال بمصدر المصحف المعتمد (رمز الخطأ: ${response.code})")
                }

                val bodyString = response.body?.string() ?: throw IOException("استجابة فارغة من خادم المصحف")
                val root = JSONObject(bodyString)
                val versesArray = root.getJSONArray("verses")

                if (versesArray.length() == 0) {
                    hasMore = false
                } else {
                    for (i in 0 until versesArray.length()) {
                        val verseObj = versesArray.getJSONObject(i)
                        val verseKey = verseObj.getString("verse_key") // e.g. "2:1"
                        val ayahNumInSurah = verseKey.substringAfter(":").toIntOrNull() ?: (ayahsList.size + 1)
                        val textUthmani = verseObj.getString("text_uthmani")
                        val verseId = verseObj.optInt("id", ayahsList.size + 1)

                        ayahsList.add(
                            QuranAyahEntity(
                                surahNumber = surahNumber,
                                ayahNumberInSurah = ayahNumInSurah,
                                ayahNumberOverall = verseId,
                                textUthmani = textUthmani,
                                pageNumber = surah.startPage,
                                juzNumber = 1,
                                sajda = false
                            )
                        )
                    }

                    val pagination = root.optJSONObject("pagination")
                    val nextPage = pagination?.optInt("next_page", 0) ?: 0
                    if (nextPage > page) {
                        page = nextPage
                    } else {
                        hasMore = false
                    }
                }
            }

            if (ayahsList.isNotEmpty()) {
                quranDao.insertAyahs(ayahsList)
            }
            return@withContext ayahsList
        } catch (e: IOException) {
            throw IOException("تعذر تنزيل آيات السورة، يرجى التحقق من اتصال الإنترنت وإعادة المحاولة.", e)
        }
    }
}
