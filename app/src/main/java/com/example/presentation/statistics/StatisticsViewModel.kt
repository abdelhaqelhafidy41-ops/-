package com.example.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.AppContainer
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.KhatmahEntity
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.AchievementRepository
import com.example.data.repository.KhatmahRepository
import com.example.data.repository.MemorizationRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeeklyDayStat(
    val dayNameArabic: String,
    val dateString: String,
    val prayersCount: Int,
    val dhikrCount: Int,
    val ayahsRead: Int
)

class StatisticsViewModel(
    private val achievementRepository: AchievementRepository,
    private val khatmahRepository: KhatmahRepository,
    private val memorizationRepository: MemorizationRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val todayAchievement: StateFlow<DailyAchievementEntity?> = achievementRepository.getTodayAchievement()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val last30Days: StateFlow<List<DailyAchievementEntity>> = achievementRepository.getLast30DaysAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievements: StateFlow<List<DailyAchievementEntity>> = achievementRepository.getAllAchievementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeKhatmahs: StateFlow<List<KhatmahEntity>> = khatmahRepository.getActiveKhatmahs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memorizationRecords: StateFlow<List<MemorizationEntity>> = memorizationRepository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _streakDays = MutableStateFlow(0)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    init {
        refreshStreak()
    }

    fun refreshStreak() {
        viewModelScope.launch {
            _streakDays.value = achievementRepository.calculateStreakDays()
        }
    }

    fun togglePrayer(prayerIndex: Int) {
        viewModelScope.launch {
            achievementRepository.togglePrayerCompleted(prayerIndex)
            refreshStreak()
        }
    }

    suspend fun getExportJson(): String {
        return achievementRepository.exportUserDataJson()
    }

    suspend fun getExportCsv(): String {
        return achievementRepository.exportUserDataCsv()
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            achievementRepository.clearAllUserData()
            _streakDays.value = 0
            onComplete()
        }
    }

    companion object {
        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatisticsViewModel(
                    achievementRepository = appContainer.achievementRepository,
                    khatmahRepository = appContainer.khatmahRepository,
                    memorizationRepository = appContainer.memorizationRepository,
                    preferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
        }
    }
}
