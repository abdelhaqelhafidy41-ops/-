package com.example.core.config

/**
 * AppConfig serves as the single source of truth for the app's identity,
 * metadata, and foundational constants. Changing values here adapts the app name
 * and core behavior across the system.
 */
object AppConfig {
    const val APP_NAME = "نور المسلم"
    const val APP_NAME_EN = "Noor Al-Muslim"
    const val APP_VERSION = "1.0.0"
    const val DATABASE_NAME = "noor_al_muslim.db"
    const val DATASTORE_PREFS_NAME = "user_preferences"
    const val DEFAULT_LOCALE = "ar"
    
    // Official Quran verified source metadata
    const val VERIFIED_QURAN_SOURCE_NAME = "مجمع الملك فهد لطباعة المصحف الشريف"
    const val VERIFIED_QURAN_SOURCE_URL = "https://qurancomplex.gov.sa"
}
