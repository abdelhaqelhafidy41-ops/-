package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.config.AppConfig
import com.example.data.local.dao.DailyAchievementDao
import com.example.data.local.dao.DhikrDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.KhatmahDao
import com.example.data.local.dao.LibraryDao
import com.example.data.local.dao.MemorizationDao
import com.example.data.local.dao.PrayerTimeDao
import com.example.data.local.dao.QuranDao
import com.example.data.local.dao.ReadingProgressDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.dao.WallpaperDao
import com.example.data.local.dao.WidgetConfigDao
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

@Database(
    entities = [
        UserSettingsEntity::class,
        FavoriteEntity::class,
        ReadingProgressEntity::class,
        DhikrEntity::class,
        DhikrHistoryEntity::class,
        DailyAchievementEntity::class,
        PrayerTimeEntity::class,
        QuranSurahEntity::class,
        QuranAyahEntity::class,
        KhatmahEntity::class,
        KhatmahLogEntity::class,
        MemorizationEntity::class,
        MemorizationQuizResultEntity::class,
        IslamicLibraryEntity::class,
        TasbeehStateEntity::class,
        WallpaperEntity::class,
        WidgetConfigEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun dailyAchievementDao(): DailyAchievementDao
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun quranDao(): QuranDao
    abstract fun khatmahDao(): KhatmahDao
    abstract fun memorizationDao(): MemorizationDao
    abstract fun libraryDao(): LibraryDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun widgetConfigDao(): WidgetConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `khatmahs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetDays` INTEGER NOT NULL,
                        `startDate` TEXT NOT NULL,
                        `targetEndDate` TEXT NOT NULL,
                        `targetType` TEXT NOT NULL,
                        `currentProgress` INTEGER NOT NULL,
                        `totalTarget` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `isPaused` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `khatmah_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `khatmahId` INTEGER NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `amountRead` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_khatmah_logs_khatmahId` ON `khatmah_logs` (`khatmahId`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `memorization_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `surahNumber` INTEGER NOT NULL,
                        `surahName` TEXT NOT NULL,
                        `startAyah` INTEGER NOT NULL,
                        `endAyah` INTEGER NOT NULL,
                        `masteryLevel` INTEGER NOT NULL,
                        `lastReviewedDate` TEXT NOT NULL,
                        `nextReviewDate` TEXT NOT NULL,
                        `reviewCount` INTEGER NOT NULL,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_memorization_records_surahNumber_startAyah_endAyah` ON `memorization_records` (`surahNumber`, `startAyah`, `endAyah`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `memorization_quiz_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `surahNumber` INTEGER NOT NULL,
                        `surahName` TEXT NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `correctAnswers` INTEGER NOT NULL,
                        `percentage` INTEGER NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_memorization_quiz_results_timestamp` ON `memorization_quiz_results` (`timestamp`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `islamic_library_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `bookId` TEXT NOT NULL,
                        `bookName` TEXT NOT NULL,
                        `author` TEXT NOT NULL,
                        `chapterName` TEXT NOT NULL,
                        `textArabic` TEXT NOT NULL,
                        `hadithNumber` TEXT,
                        `grade` TEXT,
                        `reference` TEXT NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_islamic_library_items_bookId` ON `islamic_library_items` (`bookId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_islamic_library_items_chapterName` ON `islamic_library_items` (`chapterName`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tasbeeh_state` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `phrase` TEXT NOT NULL,
                        `count` INTEGER NOT NULL,
                        `target` INTEGER NOT NULL,
                        `vibrationEnabled` INTEGER NOT NULL,
                        `lastUpdated` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wallpapers` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `titleArabic` TEXT NOT NULL,
                        `titleEnglish` TEXT,
                        `category` TEXT NOT NULL,
                        `imageUrl` TEXT NOT NULL,
                        `thumbnailUrl` TEXT NOT NULL,
                        `width` INTEGER NOT NULL,
                        `height` INTEGER NOT NULL,
                        `screenTarget` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `designerOrAuthor` TEXT NOT NULL,
                        `licenseType` TEXT NOT NULL,
                        `attributionText` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `isDownloaded` INTEGER NOT NULL,
                        `localFilePath` TEXT,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_wallpapers_category` ON `wallpapers` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_wallpapers_isFavorite` ON `wallpapers` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_wallpapers_isDownloaded` ON `wallpapers` (`isDownloaded`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `widget_configs` (
                        `appWidgetId` INTEGER PRIMARY KEY NOT NULL,
                        `widgetType` TEXT NOT NULL,
                        `cityName` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `calculationMethod` TEXT NOT NULL,
                        `madhhab` TEXT NOT NULL,
                        `themeMode` TEXT NOT NULL,
                        `showHijriDate` INTEGER NOT NULL,
                        `preferredReciter` TEXT NOT NULL,
                        `customPhrase` TEXT NOT NULL,
                        `fontSizeMultiplier` REAL NOT NULL,
                        `lastUpdated` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConfig.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun buildInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
