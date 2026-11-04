package com.example.data.importer

import android.content.Context
import com.example.data.local.dao.DhikrDao
import com.example.data.local.dao.LibraryDao
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.IslamicLibraryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AdhkarDataImporter(
    private val context: Context,
    private val dhikrDao: DhikrDao,
    private val libraryDao: LibraryDao
) {

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val dhikrCount = dhikrDao.getDhikrCount()
        if (dhikrCount < 20) {
            importAdhkarFromAssets()
        }

        val libraryCount = libraryDao.getLibraryCount()
        if (libraryCount < 10) {
            importLibraryFromAssets()
        }
    }

    private suspend fun importAdhkarFromAssets() {
        try {
            val jsonString = context.assets.open("adhkar/verified_adhkar.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<DhikrEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    DhikrEntity(
                        category = obj.getString("category"),
                        title = obj.getString("title"),
                        arabicText = obj.getString("arabicText"),
                        reference = if (obj.has("reference")) obj.getString("reference") else null,
                        grade = if (obj.has("grade")) obj.getString("grade") else "صحيح",
                        translation = if (obj.has("translation")) obj.getString("translation") else null,
                        repetitionTarget = obj.optInt("targetCount", 1),
                        rewardNote = if (obj.has("virtue")) obj.getString("virtue") else null,
                        orderIndex = obj.optInt("orderIndex", i + 1)
                    )
                )
            }
            if (items.isNotEmpty()) {
                dhikrDao.insertDhikrItems(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun importLibraryFromAssets() {
        try {
            val jsonString = context.assets.open("library/verified_library.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<IslamicLibraryEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    IslamicLibraryEntity(
                        bookId = obj.getString("bookId"),
                        bookName = obj.getString("bookName"),
                        author = obj.getString("author"),
                        chapterName = obj.getString("chapterName"),
                        textArabic = obj.getString("textArabic"),
                        hadithNumber = if (obj.has("hadithNumber")) obj.getString("hadithNumber") else null,
                        grade = if (obj.has("grade")) obj.getString("grade") else "صحيح",
                        reference = obj.getString("reference"),
                        orderIndex = obj.optInt("orderIndex", i + 1)
                    )
                )
            }
            if (items.isNotEmpty()) {
                libraryDao.insertItems(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
