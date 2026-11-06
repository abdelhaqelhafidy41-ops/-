package com.example.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.AppContainer
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontFamilyType
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.AchievementRepository
import com.example.data.repository.DhikrRepository
import com.example.data.repository.LibraryRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OfflineStorageStats(
    val downloadedSurahsCount: Int = 114,
    val adhkarCount: Int = 120,
    val libraryItemsCount: Int = 100
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val achievementRepository: AchievementRepository,
    private val quranRepository: QuranRepository,
    private val dhikrRepository: DhikrRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _offlineStats = MutableStateFlow(OfflineStorageStats())
    val offlineStats: StateFlow<OfflineStorageStats> = _offlineStats.asStateFlow()

    init {
        loadOfflineStats()
    }

    private fun loadOfflineStats() {
        viewModelScope.launch {
            val dhikrCount = dhikrRepository.getDhikrCount()
            val libCount = libraryRepository.getLibraryCount()
            _offlineStats.value = OfflineStorageStats(
                downloadedSurahsCount = 114,
                adhkarCount = if (dhikrCount > 0) dhikrCount else 120,
                libraryItemsCount = if (libCount > 0) libCount else 100
            )
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(themeMode)
        }
    }

    fun setFontSizeScale(scale: FontSizeScale) {
        viewModelScope.launch {
            preferencesRepository.updateFontSizeScale(scale)
        }
    }

    fun setFontFamilyType(type: FontFamilyType) {
        viewModelScope.launch {
            preferencesRepository.updateFontFamilyType(type)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesRepository.updateLanguage(language)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch {
            preferencesRepository.updateCalculationMethod(method)
        }
    }

    fun setMadhhab(madhhab: Madhhab) {
        viewModelScope.launch {
            preferencesRepository.updateMadhhab(madhhab)
        }
    }

    fun setAudioPreference(audioPreference: AudioPreference) {
        viewModelScope.launch {
            preferencesRepository.updateAudioPreference(audioPreference)
        }
    }

    fun setPreferredReciter(reciterId: String) {
        viewModelScope.launch {
            preferencesRepository.updatePreferredReciter(reciterId)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateVibrationEnabled(enabled)
        }
    }

    fun setDailyDhikrReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDailyDhikrReminderEnabled(enabled)
        }
    }

    fun setPreAdhanMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updatePreAdhanMinutes(minutes)
        }
    }

    fun setPrayerNotification(prayerKey: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePrayerNotification(prayerKey, enabled)
        }
    }

    fun resetManualOffsets() {
        viewModelScope.launch {
            preferencesRepository.updateManualOffsets(0, 0, 0, 0, 0, 0)
        }
    }

    fun resetToDefaults(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
            onComplete()
        }
    }

    fun clearAllUserData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            achievementRepository.clearAllUserData()
            onComplete()
        }
    }

    companion object {
        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    preferencesRepository = appContainer.userPreferencesRepository,
                    achievementRepository = appContainer.achievementRepository,
                    quranRepository = appContainer.quranRepository,
                    dhikrRepository = appContainer.dhikrRepository,
                    libraryRepository = appContainer.libraryRepository
                ) as T
            }
        }
    }
}
