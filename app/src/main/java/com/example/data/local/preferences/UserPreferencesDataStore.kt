package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.config.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConfig.DATASTORE_PREFS_NAME)

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.dataStore)

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE_SCALE = stringPreferencesKey("font_size_scale")
        val FONT_FAMILY_TYPE = stringPreferencesKey("font_family_type")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val MADHHAB = stringPreferencesKey("madhhab")
        val AUDIO_PREFERENCE = stringPreferencesKey("audio_preference")
        val PREFERRED_RECITER = stringPreferencesKey("preferred_reciter")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DAILY_DHIKR_REMINDER = booleanPreferencesKey("daily_dhikr_reminder")

        val LAST_LATITUDE = doublePreferencesKey("last_latitude")
        val LAST_LONGITUDE = doublePreferencesKey("last_longitude")
        val LAST_CITY_NAME = stringPreferencesKey("last_city_name")
        val HAS_LOCATION_PERMISSION = booleanPreferencesKey("has_location_permission")

        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_SUNRISE = booleanPreferencesKey("notify_sunrise")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")

        val PRE_ADHAN_MINUTES = intPreferencesKey("pre_adhan_minutes")

        val OFFSET_FAJR = intPreferencesKey("offset_fajr")
        val OFFSET_SUNRISE = intPreferencesKey("offset_sunrise")
        val OFFSET_DHUHR = intPreferencesKey("offset_dhuhr")
        val OFFSET_ASR = intPreferencesKey("offset_asr")
        val OFFSET_MAGHRIB = intPreferencesKey("offset_maghrib")
        val OFFSET_ISHA = intPreferencesKey("offset_isha")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeMode = runCatching {
                ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM)

            val fontSizeScale = runCatching {
                FontSizeScale.valueOf(preferences[PreferencesKeys.FONT_SIZE_SCALE] ?: FontSizeScale.NORMAL.name)
            }.getOrDefault(FontSizeScale.NORMAL)

            val fontFamilyType = runCatching {
                FontFamilyType.valueOf(preferences[PreferencesKeys.FONT_FAMILY_TYPE] ?: FontFamilyType.CAIRO.name)
            }.getOrDefault(FontFamilyType.CAIRO)

            val language = preferences[PreferencesKeys.LANGUAGE] ?: AppConfig.DEFAULT_LOCALE
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false

            val calculationMethod = runCatching {
                CalculationMethod.valueOf(preferences[PreferencesKeys.CALCULATION_METHOD] ?: CalculationMethod.UMM_AL_QURA.name)
            }.getOrDefault(CalculationMethod.UMM_AL_QURA)

            val madhhab = runCatching {
                Madhhab.valueOf(preferences[PreferencesKeys.MADHHAB] ?: Madhhab.STANDARD.name)
            }.getOrDefault(Madhhab.STANDARD)

            val audioPreference = runCatching {
                AudioPreference.valueOf(preferences[PreferencesKeys.AUDIO_PREFERENCE] ?: AudioPreference.ADHAN.name)
            }.getOrDefault(AudioPreference.ADHAN)

            val preferredReciter = preferences[PreferencesKeys.PREFERRED_RECITER] ?: "Alafasy_128kbps"
            val vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
            val dailyDhikrReminder = preferences[PreferencesKeys.DAILY_DHIKR_REMINDER] ?: true

            val lastLat = preferences[PreferencesKeys.LAST_LATITUDE] ?: 21.4225
            val lastLon = preferences[PreferencesKeys.LAST_LONGITUDE] ?: 39.8262
            val lastCity = preferences[PreferencesKeys.LAST_CITY_NAME] ?: "مكة المكرمة"
            val hasLocPerm = preferences[PreferencesKeys.HAS_LOCATION_PERMISSION] ?: false

            val notifyFajr = preferences[PreferencesKeys.NOTIFY_FAJR] ?: true
            val notifySunrise = preferences[PreferencesKeys.NOTIFY_SUNRISE] ?: false
            val notifyDhuhr = preferences[PreferencesKeys.NOTIFY_DHUHR] ?: true
            val notifyAsr = preferences[PreferencesKeys.NOTIFY_ASR] ?: true
            val notifyMaghrib = preferences[PreferencesKeys.NOTIFY_MAGHRIB] ?: true
            val notifyIsha = preferences[PreferencesKeys.NOTIFY_ISHA] ?: true

            val preAdhanMinutes = preferences[PreferencesKeys.PRE_ADHAN_MINUTES] ?: 10

            val offsetFajr = preferences[PreferencesKeys.OFFSET_FAJR] ?: 0
            val offsetSunrise = preferences[PreferencesKeys.OFFSET_SUNRISE] ?: 0
            val offsetDhuhr = preferences[PreferencesKeys.OFFSET_DHUHR] ?: 0
            val offsetAsr = preferences[PreferencesKeys.OFFSET_ASR] ?: 0
            val offsetMaghrib = preferences[PreferencesKeys.OFFSET_MAGHRIB] ?: 0
            val offsetIsha = preferences[PreferencesKeys.OFFSET_ISHA] ?: 0

            UserPreferences(
                themeMode = themeMode,
                fontSizeScale = fontSizeScale,
                fontFamilyType = fontFamilyType,
                language = language,
                notificationsEnabled = notificationsEnabled,
                calculationMethod = calculationMethod,
                madhhab = madhhab,
                audioPreference = audioPreference,
                preferredReciter = preferredReciter,
                vibrationEnabled = vibrationEnabled,
                dailyDhikrReminderEnabled = dailyDhikrReminder,
                lastLatitude = lastLat,
                lastLongitude = lastLon,
                lastCityName = lastCity,
                hasLocationPermission = hasLocPerm,
                notifyFajr = notifyFajr,
                notifySunrise = notifySunrise,
                notifyDhuhr = notifyDhuhr,
                notifyAsr = notifyAsr,
                notifyMaghrib = notifyMaghrib,
                notifyIsha = notifyIsha,
                preAdhanMinutes = preAdhanMinutes,
                offsetFajr = offsetFajr,
                offsetSunrise = offsetSunrise,
                offsetDhuhr = offsetDhuhr,
                offsetAsr = offsetAsr,
                offsetMaghrib = offsetMaghrib,
                offsetIsha = offsetIsha
            )
        }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateFontSizeScale(fontSizeScale: FontSizeScale) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE_SCALE] = fontSizeScale.name
        }
    }

    suspend fun updateFontFamilyType(fontFamilyType: FontFamilyType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY_TYPE] = fontFamilyType.name
        }
    }

    suspend fun updateLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateCalculationMethod(method: CalculationMethod) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALCULATION_METHOD] = method.name
        }
    }

    suspend fun updateMadhhab(madhhab: Madhhab) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MADHHAB] = madhhab.name
        }
    }

    suspend fun updateAudioPreference(audioPreference: AudioPreference) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_PREFERENCE] = audioPreference.name
        }
    }

    suspend fun updatePreferredReciter(reciterId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_RECITER] = reciterId
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateDailyDhikrReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_DHIKR_REMINDER] = enabled
        }
    }

    suspend fun updateLastLocation(latitude: Double, longitude: Double, cityName: String, hasPermission: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_LATITUDE] = latitude
            preferences[PreferencesKeys.LAST_LONGITUDE] = longitude
            preferences[PreferencesKeys.LAST_CITY_NAME] = cityName
            preferences[PreferencesKeys.HAS_LOCATION_PERMISSION] = hasPermission
        }
    }

    suspend fun updatePrayerNotification(prayerKey: String, enabled: Boolean) {
        dataStore.edit { preferences ->
            when (prayerKey.lowercase()) {
                "fajr" -> preferences[PreferencesKeys.NOTIFY_FAJR] = enabled
                "sunrise" -> preferences[PreferencesKeys.NOTIFY_SUNRISE] = enabled
                "dhuhr" -> preferences[PreferencesKeys.NOTIFY_DHUHR] = enabled
                "asr" -> preferences[PreferencesKeys.NOTIFY_ASR] = enabled
                "maghrib" -> preferences[PreferencesKeys.NOTIFY_MAGHRIB] = enabled
                "isha" -> preferences[PreferencesKeys.NOTIFY_ISHA] = enabled
            }
        }
    }

    suspend fun updatePreAdhanMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRE_ADHAN_MINUTES] = minutes
        }
    }

    suspend fun updateManualOffsets(
        fajr: Int,
        sunrise: Int,
        dhuhr: Int,
        asr: Int,
        maghrib: Int,
        isha: Int
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFSET_FAJR] = fajr
            preferences[PreferencesKeys.OFFSET_SUNRISE] = sunrise
            preferences[PreferencesKeys.OFFSET_DHUHR] = dhuhr
            preferences[PreferencesKeys.OFFSET_ASR] = asr
            preferences[PreferencesKeys.OFFSET_MAGHRIB] = maghrib
            preferences[PreferencesKeys.OFFSET_ISHA] = isha
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
