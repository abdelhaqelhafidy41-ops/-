package com.example.core.util

import android.content.Context
import com.example.data.local.database.AppDatabase
import com.example.data.local.preferences.UserPreferencesDataStore
import com.example.data.repository.AchievementRepository
import com.example.data.repository.AchievementRepositoryImpl
import com.example.data.repository.DhikrRepository
import com.example.data.repository.DhikrRepositoryImpl
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.FavoritesRepositoryImpl
import com.example.data.repository.PrayerTimesRepository
import com.example.data.repository.PrayerTimesRepositoryImpl
import com.example.data.repository.QuranRepository
import com.example.audio.QuranAudioPlayer
import com.example.data.importer.QuranDataImporter
import com.example.data.repository.KhatmahRepository
import com.example.data.repository.KhatmahRepositoryImpl
import com.example.data.repository.MemorizationRepository
import com.example.data.repository.MemorizationRepositoryImpl
import com.example.data.repository.QuranRepositoryImpl
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.UserPreferencesRepositoryImpl

interface AppContainer {
    val database: AppDatabase
    val userPreferencesDataStore: UserPreferencesDataStore
    val quranRepository: QuranRepository
    val khatmahRepository: KhatmahRepository
    val memorizationRepository: MemorizationRepository
    val audioPlayer: QuranAudioPlayer
    val dhikrRepository: DhikrRepository
    val libraryRepository: com.example.data.repository.LibraryRepository
    val prayerTimesRepository: PrayerTimesRepository
    val favoritesRepository: FavoritesRepository
    val achievementRepository: AchievementRepository
    val userPreferencesRepository: UserPreferencesRepository
    val locationHelper: com.example.core.location.LocationHelper
    val compassSensorManager: com.example.core.prayer.CompassSensorManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val userPreferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(context)
    }

    private val quranDataImporter: QuranDataImporter by lazy {
        QuranDataImporter(context, database.quranDao())
    }

    override val quranRepository: QuranRepository by lazy {
        QuranRepositoryImpl(
            quranDao = database.quranDao(),
            readingProgressDao = database.readingProgressDao(),
            dataImporter = quranDataImporter
        )
    }

    override val khatmahRepository: KhatmahRepository by lazy {
        KhatmahRepositoryImpl(
            khatmahDao = database.khatmahDao()
        )
    }

    override val memorizationRepository: MemorizationRepository by lazy {
        MemorizationRepositoryImpl(
            memorizationDao = database.memorizationDao()
        )
    }

    override val audioPlayer: QuranAudioPlayer by lazy {
        QuranAudioPlayer(context.applicationContext)
    }

    private val adhkarDataImporter: com.example.data.importer.AdhkarDataImporter by lazy {
        com.example.data.importer.AdhkarDataImporter(
            context = context,
            dhikrDao = database.dhikrDao(),
            libraryDao = database.libraryDao()
        )
    }

    override val dhikrRepository: DhikrRepository by lazy {
        DhikrRepositoryImpl(
            dhikrDao = database.dhikrDao(),
            dataImporter = adhkarDataImporter
        )
    }

    override val libraryRepository: com.example.data.repository.LibraryRepository by lazy {
        com.example.data.repository.LibraryRepositoryImpl(
            libraryDao = database.libraryDao()
        )
    }

    override val prayerTimesRepository: PrayerTimesRepository by lazy {
        PrayerTimesRepositoryImpl(
            prayerTimeDao = database.prayerTimeDao()
        )
    }

    override val favoritesRepository: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(
            favoriteDao = database.favoriteDao()
        )
    }

    override val achievementRepository: AchievementRepository by lazy {
        AchievementRepositoryImpl(
            dailyAchievementDao = database.dailyAchievementDao()
        )
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(
            dataStore = userPreferencesDataStore,
            userSettingsDao = database.userSettingsDao()
        )
    }

    override val locationHelper: com.example.core.location.LocationHelper by lazy {
        com.example.core.location.LocationHelper(context)
    }

    override val compassSensorManager: com.example.core.prayer.CompassSensorManager by lazy {
        com.example.core.prayer.CompassSensorManager(context)
    }
}
