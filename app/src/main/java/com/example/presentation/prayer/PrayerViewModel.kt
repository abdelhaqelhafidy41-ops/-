package com.example.presentation.prayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.location.LocationHelper
import com.example.core.prayer.CompassReading
import com.example.core.prayer.CompassSensorManager
import com.example.core.prayer.NextPrayerInfo
import com.example.core.prayer.PrayerCalculator
import com.example.core.prayer.PrayerOffsets
import com.example.core.prayer.PrayerSchedule
import com.example.core.prayer.QiblaHelper
import com.example.core.prayer.SensorAccuracyLevel
import com.example.core.util.AppContainer
import com.example.data.local.entity.PrayerTimeEntity
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.PrayerTimesRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.notifications.PrayerAlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerViewModel(
    private val appContext: Context,
    private val prayerTimesRepository: PrayerTimesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val locationHelper: LocationHelper,
    private val compassSensorManager: CompassSensorManager
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0 = Times, 1 = Qibla
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _dayOffset = MutableStateFlow(0) // 0 = today, -1 = yesterday, +1 = tomorrow
    val dayOffset: StateFlow<Int> = _dayOffset.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _isUsingSavedLocation = MutableStateFlow(true)
    val isUsingSavedLocation: StateFlow<Boolean> = _isUsingSavedLocation.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _currentTimeTicker = MutableStateFlow(System.currentTimeMillis())
    val currentTimeTicker: StateFlow<Long> = _currentTimeTicker.asStateFlow()

    // Compass state
    private val _compassReading = MutableStateFlow(
        CompassReading(0f, SensorAccuracyLevel.HIGH, compassSensorManager.isAvailable)
    )
    val compassReading: StateFlow<CompassReading> = _compassReading.asStateFlow()

    init {
        // Ticker loop for countdown (updates every second)
        viewModelScope.launch {
            while (isActive) {
                _currentTimeTicker.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        // Compass reading stream
        viewModelScope.launch {
            compassSensorManager.getCompassUpdates().collect { reading ->
                _compassReading.value = reading
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun previousDay() {
        _dayOffset.value -= 1
    }

    fun nextDay() {
        _dayOffset.value += 1
    }

    fun resetToToday() {
        _dayOffset.value = 0
    }

    fun onLocationPermissionGranted() {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _isLocating.value = true
            val loc = locationHelper.getCurrentLocation()
            _isLocating.value = false

            if (loc != null) {
                _isUsingSavedLocation.value = false
                userPreferencesRepository.updateLastLocation(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    cityName = loc.cityName,
                    hasPermission = true
                )

                // Cache today's prayer times to Room
                val prefs = userPreferences.value
                val schedule = PrayerCalculator.calculatePrayerTimes(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    date = Date(),
                    method = prefs.calculationMethod,
                    madhhab = prefs.madhhab,
                    offsets = PrayerOffsets(
                        fajr = prefs.offsetFajr,
                        sunrise = prefs.offsetSunrise,
                        dhuhr = prefs.offsetDhuhr,
                        asr = prefs.offsetAsr,
                        maghrib = prefs.offsetMaghrib,
                        isha = prefs.offsetIsha
                    ),
                    cityName = loc.cityName
                )
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
                val entity = PrayerTimeEntity(
                    dateString = sdf.format(Date()),
                    cityName = loc.cityName,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    fajrTime = timeFormat.format(schedule.fajr),
                    sunriseTime = timeFormat.format(schedule.sunrise),
                    dhuhrTime = timeFormat.format(schedule.dhuhr),
                    asrTime = timeFormat.format(schedule.asr),
                    maghribTime = timeFormat.format(schedule.maghrib),
                    ishaTime = timeFormat.format(schedule.isha),
                    calculationMethod = prefs.calculationMethod.name
                )
                prayerTimesRepository.savePrayerTimes(entity)

                // Reschedule alarms with new location
                PrayerAlarmScheduler.scheduleAllPrayerAlarms(appContext, prefs)
            } else {
                _isUsingSavedLocation.value = true
            }
        }
    }

    fun togglePrayerNotification(prayerKey: String, enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePrayerNotification(prayerKey, enabled)
            val updatedPrefs = userPreferences.value
            PrayerAlarmScheduler.scheduleAllPrayerAlarms(appContext, updatedPrefs)
        }
    }

    fun updateManualOffset(prayerKey: String, delta: Int) {
        val prefs = userPreferences.value
        val newFajr = if (prayerKey == "fajr") (prefs.offsetFajr + delta).coerceIn(-60, 60) else prefs.offsetFajr
        val newSunrise = if (prayerKey == "sunrise") (prefs.offsetSunrise + delta).coerceIn(-60, 60) else prefs.offsetSunrise
        val newDhuhr = if (prayerKey == "dhuhr") (prefs.offsetDhuhr + delta).coerceIn(-60, 60) else prefs.offsetDhuhr
        val newAsr = if (prayerKey == "asr") (prefs.offsetAsr + delta).coerceIn(-60, 60) else prefs.offsetAsr
        val newMaghrib = if (prayerKey == "maghrib") (prefs.offsetMaghrib + delta).coerceIn(-60, 60) else prefs.offsetMaghrib
        val newIsha = if (prayerKey == "isha") (prefs.offsetIsha + delta).coerceIn(-60, 60) else prefs.offsetIsha

        viewModelScope.launch {
            userPreferencesRepository.updateManualOffsets(
                fajr = newFajr,
                sunrise = newSunrise,
                dhuhr = newDhuhr,
                asr = newAsr,
                maghrib = newMaghrib,
                isha = newIsha
            )
            PrayerAlarmScheduler.scheduleAllPrayerAlarms(appContext, userPreferences.value)
        }
    }

    fun calculateScheduleForOffset(offsetDays: Int, prefs: UserPreferences): PrayerSchedule {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offsetDays)
        }
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
            date = cal.time,
            method = prefs.calculationMethod,
            madhhab = prefs.madhhab,
            offsets = offsets,
            cityName = prefs.lastCityName
        )
    }

    fun getNextPrayer(prefs: UserPreferences): NextPrayerInfo {
        val today = calculateScheduleForOffset(0, prefs)
        val tomorrow = calculateScheduleForOffset(1, prefs)
        return PrayerCalculator.getNextPrayer(today, tomorrow, Date())
    }

    fun getQiblaBearing(prefs: UserPreferences): Float {
        return QiblaHelper.calculateQiblaBearing(prefs.lastLatitude, prefs.lastLongitude)
    }

    fun getDistanceToKaabaKm(prefs: UserPreferences): Double {
        return QiblaHelper.calculateDistanceToKaabaKm(prefs.lastLatitude, prefs.lastLongitude)
    }

    companion object {
        fun Factory(appContext: Context, appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PrayerViewModel(
                        appContext = appContext,
                        prayerTimesRepository = appContainer.prayerTimesRepository,
                        userPreferencesRepository = appContainer.userPreferencesRepository,
                        locationHelper = appContainer.locationHelper,
                        compassSensorManager = appContainer.compassSensorManager
                    ) as T
                }
            }
    }
}
