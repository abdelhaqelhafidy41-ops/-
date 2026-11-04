package com.example.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.AppContainer
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

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

    companion object {
        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    preferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
        }
    }
}
