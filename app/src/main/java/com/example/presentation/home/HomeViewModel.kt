package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.prayer.HijriCalendarHelper
import com.example.core.prayer.HijriDate
import com.example.core.prayer.NextPrayerInfo
import com.example.core.prayer.PrayerCalculator
import com.example.core.prayer.PrayerOffsets
import com.example.core.prayer.PrayerSchedule
import com.example.core.util.AppContainer
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.AchievementRepository
import com.example.data.repository.DhikrRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val quranRepository: QuranRepository,
    private val dhikrRepository: DhikrRepository,
    private val achievementRepository: AchievementRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> =
        userPreferencesRepository.userPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPreferences()
            )

    val readingProgress: StateFlow<ReadingProgressEntity?> =
        quranRepository.getReadingProgress()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    val todayDhikrCount: StateFlow<Int?> =
        dhikrRepository.getTodayDhikrCount()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    val dailyAchievement: StateFlow<DailyAchievementEntity?> =
        achievementRepository.getTodayAchievement()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    private val _ticker = MutableStateFlow(System.currentTimeMillis())
    val ticker: StateFlow<Long> = _ticker.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _ticker.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    fun getTodaySchedule(prefs: UserPreferences): PrayerSchedule {
        val offsets = PrayerOffsets(
            fajr = prefs.offsetFajr,
            sunrise = prefs.offsetSunrise,
            dhuhr = prefs.offsetDhuhr,
            asr = prefs.offsetAsr,
            maghrib = prefs.offsetMaghrib,
            isha = prefs.offsetIsha
        )
        return PrayerCalculator.calculatePrayerTimes(
            latitude = prefs.lastLatitude,
            longitude = prefs.lastLongitude,
            date = Date(),
            method = prefs.calculationMethod,
            madhhab = prefs.madhhab,
            offsets = offsets,
            cityName = prefs.lastCityName
        )
    }

    fun getNextPrayer(prefs: UserPreferences): NextPrayerInfo {
        val offsets = PrayerOffsets(
            fajr = prefs.offsetFajr,
            sunrise = prefs.offsetSunrise,
            dhuhr = prefs.offsetDhuhr,
            asr = prefs.offsetAsr,
            maghrib = prefs.offsetMaghrib,
            isha = prefs.offsetIsha
        )
        val today = PrayerCalculator.calculatePrayerTimes(
            latitude = prefs.lastLatitude,
            longitude = prefs.lastLongitude,
            date = Date(),
            method = prefs.calculationMethod,
            madhhab = prefs.madhhab,
            offsets = offsets,
            cityName = prefs.lastCityName
        )
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
        val tomorrow = PrayerCalculator.calculatePrayerTimes(
            latitude = prefs.lastLatitude,
            longitude = prefs.lastLongitude,
            date = cal.time,
            method = prefs.calculationMethod,
            madhhab = prefs.madhhab,
            offsets = offsets,
            cityName = prefs.lastCityName
        )
        return PrayerCalculator.getNextPrayer(today, tomorrow, Date())
    }

    fun getHijriDate(): HijriDate {
        return HijriCalendarHelper.getHijriDate(Date())
    }

    fun getGregorianDate(): String {
        return HijriCalendarHelper.getFormattedGregorianDate(Date())
    }

    companion object {
        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    quranRepository = appContainer.quranRepository,
                    dhikrRepository = appContainer.dhikrRepository,
                    achievementRepository = appContainer.achievementRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
        }
    }
}
