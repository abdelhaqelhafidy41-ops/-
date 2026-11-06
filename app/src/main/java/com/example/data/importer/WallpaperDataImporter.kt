package com.example.data.importer

import android.content.Context
import com.example.data.local.dao.WallpaperDao
import com.example.data.local.entity.WallpaperEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class WallpaperDataImporter(
    private val context: Context,
    private val wallpaperDao: WallpaperDao
) {
    suspend fun seedWallpapersIfNeeded() = withContext(Dispatchers.IO) {
        val count = wallpaperDao.getWallpapersCount()
        if (count < 100) {
            importWallpapersFromAssets()
        }
    }

    private suspend fun importWallpapersFromAssets() {
        try {
            val jsonString = context.assets.open("wallpapers/verified_wallpapers.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<WallpaperEntity>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                // Check if existing wallpaper preserves user favorite or download state
                val existing = wallpaperDao.getWallpaperById(id)
                items.add(
                    WallpaperEntity(
                        id = id,
                        titleArabic = obj.getString("titleArabic"),
                        titleEnglish = if (obj.has("titleEnglish")) obj.getString("titleEnglish") else null,
                        category = obj.getString("category"),
                        imageUrl = obj.getString("imageUrl"),
                        thumbnailUrl = obj.getString("thumbnailUrl"),
                        width = obj.optInt("width", 1080),
                        height = obj.optInt("height", 1920),
                        screenTarget = obj.optString("screenTarget", "both"),
                        source = obj.optString("source", "Unsplash"),
                        designerOrAuthor = obj.optString("designerOrAuthor", "Islamic Heritage / Open License"),
                        licenseType = obj.optString("licenseType", "Unsplash License"),
                        attributionText = obj.optString("attributionText", "Free for commercial and personal use"),
                        isFavorite = existing?.isFavorite ?: false,
                        isDownloaded = existing?.isDownloaded ?: false,
                        localFilePath = existing?.localFilePath,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            if (items.isNotEmpty()) {
                wallpaperDao.insertWallpapers(items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
