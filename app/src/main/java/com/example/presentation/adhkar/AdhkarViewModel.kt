package com.example.presentation.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.AppContainer
import com.example.data.local.entity.DailyAchievementEntity
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.DhikrHistoryEntity
import com.example.data.local.entity.TasbeehStateEntity
import com.example.data.repository.AchievementRepository
import com.example.data.repository.DhikrRepository
import com.example.data.repository.FavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarViewModel(
    private val dhikrRepository: DhikrRepository,
    private val favoritesRepository: FavoritesRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _selectedMainTab = MutableStateFlow(0) // 0: الأذكار والأدعية, 1: السبحة الإلكترونية
    val selectedMainTab: StateFlow<Int> = _selectedMainTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("أذكار الصباح")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f) // 0.85f to 1.4f
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    // Per-Dhikr individual counters
    private val _itemCounters = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val itemCounters: StateFlow<Map<Long, Int>> = _itemCounters.asStateFlow()

    // Electronic Tasbeeh state
    private val _tasbeehPhrase = MutableStateFlow("سبحان الله وبحمده")
    val tasbeehPhrase: StateFlow<String> = _tasbeehPhrase.asStateFlow()

    private val _tasbeehCount = MutableStateFlow(0)
    val tasbeehCount: StateFlow<Int> = _tasbeehCount.asStateFlow()

    private val _tasbeehTarget = MutableStateFlow(33)
    val tasbeehTarget: StateFlow<Int> = _tasbeehTarget.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private var lastTapTime = 0L

    init {
        viewModelScope.launch {
            dhikrRepository.ensureInitialData()
            // Restore persistent Tasbeeh state from database
            dhikrRepository.getTasbeehState().firstOrNull()?.let { saved ->
                _tasbeehPhrase.value = saved.phrase
                _tasbeehCount.value = saved.count
                _tasbeehTarget.value = saved.target
                _vibrationEnabled.value = saved.vibrationEnabled
            }
        }
    }

    // Filtered Dhikr items according to search query or selected category
    val dhikrItems: StateFlow<List<DhikrEntity>> = combine(_searchQuery, _selectedCategory) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        if (query.isNotBlank()) {
            dhikrRepository.searchDhikr(query)
        } else {
            dhikrRepository.getDhikrByCategory(category)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorite items ID set
    val favoriteDhikrIds: StateFlow<Set<String>> = favoritesRepository.getFavoritesByType("DHIKR")
        .map { list -> list.map { it.itemId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val todayTotalCount: StateFlow<Int?> = dhikrRepository.getTodayDhikrCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val recentHistory: StateFlow<List<DhikrHistoryEntity>> = dhikrRepository.getRecentHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectMainTab(index: Int) {
        _selectedMainTab.value = index
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.85f, 1.4f)
    }

    fun incrementDhikr(dhikr: DhikrEntity) {
        val current = _itemCounters.value[dhikr.id] ?: 0
        val next = current + 1
        _itemCounters.value = _itemCounters.value + (dhikr.id to next)

        // Log when target is reached or progress made
        if (next == dhikr.targetCount || next % 10 == 0) {
            recordSession(dhikr.title, next, dhikr.targetCount)
        }
    }

    fun resetDhikr(dhikr: DhikrEntity) {
        val current = _itemCounters.value[dhikr.id] ?: 0
        if (current > 0) {
            recordSession(dhikr.title, current, dhikr.targetCount)
        }
        _itemCounters.value = _itemCounters.value + (dhikr.id to 0)
    }

    fun toggleFavorite(dhikr: DhikrEntity) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(
                itemType = "DHIKR",
                itemId = dhikr.id.toString(),
                title = dhikr.title,
                subtitle = dhikr.category,
                arabicText = dhikr.arabicText,
                reference = dhikr.reference
            )
        }
    }

    // Electronic Tasbeeh Actions
    fun incrementTasbeeh(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 60) {
            // Debounce accidental simultaneous taps
            return false
        }
        lastTapTime = now

        val next = _tasbeehCount.value + 1
        _tasbeehCount.value = next

        if (_tasbeehTarget.value > 0 && next % _tasbeehTarget.value == 0) {
            recordSession(_tasbeehPhrase.value, _tasbeehTarget.value, _tasbeehTarget.value)
        }

        saveTasbeehPersistence()
        return true
    }

    fun decrementTasbeeh() {
        val current = _tasbeehCount.value
        if (current > 0) {
            _tasbeehCount.value = current - 1
            saveTasbeehPersistence()
        }
    }

    fun resetTasbeeh() {
        val current = _tasbeehCount.value
        if (current > 0) {
            recordSession(_tasbeehPhrase.value, current, _tasbeehTarget.value)
        }
        _tasbeehCount.value = 0
        saveTasbeehPersistence()
    }

    fun setTasbeehConfig(phrase: String, target: Int) {
        val current = _tasbeehCount.value
        if (current > 0) {
            recordSession(_tasbeehPhrase.value, current, _tasbeehTarget.value)
        }
        _tasbeehPhrase.value = phrase
        _tasbeehTarget.value = target
        _tasbeehCount.value = 0
        saveTasbeehPersistence()
    }

    fun toggleVibration(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        saveTasbeehPersistence()
    }

    private fun saveTasbeehPersistence() {
        viewModelScope.launch {
            dhikrRepository.saveTasbeehState(
                TasbeehStateEntity(
                    id = 1,
                    phrase = _tasbeehPhrase.value,
                    count = _tasbeehCount.value,
                    target = _tasbeehTarget.value,
                    vibrationEnabled = _vibrationEnabled.value,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    private fun recordSession(title: String, count: Int, target: Int) {
        if (count <= 0) return
        viewModelScope.launch {
            dhikrRepository.recordDhikrSession(
                title = title,
                count = count,
                target = target
            )
            // Update daily achievements
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val currentAchievement = achievementRepository.getTodayAchievement().firstOrNull()
            val totalDhikr = (currentAchievement?.totalDhikrCount ?: 0) + count
            achievementRepository.updateTodayAchievement(
                DailyAchievementEntity(
                    id = currentAchievement?.id ?: 0,
                    dateString = today,
                    prayersCompletedMask = currentAchievement?.prayersCompletedMask ?: 0,
                    quranMinutesRead = currentAchievement?.quranMinutesRead ?: 0,
                    quranAyahsRead = currentAchievement?.quranAyahsRead ?: 0,
                    totalDhikrCount = totalDhikr,
                    morningDhikrCompleted = currentAchievement?.morningDhikrCompleted ?: false,
                    eveningDhikrCompleted = currentAchievement?.eveningDhikrCompleted ?: false,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    companion object {
        val CATEGORIES = listOf(
            "أذكار الصباح",
            "أذكار المساء",
            "أذكار بعد الصلاة",
            "أذكار النوم",
            "أذكار الاستيقاظ",
            "أذكار دخول وخروج المنزل",
            "أذكار المسجد",
            "أذكار السفر",
            "الرقية الشرعية",
            "أدعية القرآن",
            "أدعية الأنبياء",
            "أدعية متنوعة"
        )

        val TASBEEH_PRESETS = listOf(
            "سبحان الله وبحمده" to 33,
            "سُبْحَانَ اللَّهِ" to 33,
            "الْحَمْدُ لِلَّهِ" to 33,
            "لَا إِلَهَ إِلَّا اللَّهُ" to 100,
            "اللَّهُ أَكْبَرُ" to 33,
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ" to 100,
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" to 33,
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ" to 100
        )

        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdhkarViewModel(
                    dhikrRepository = appContainer.dhikrRepository,
                    favoritesRepository = appContainer.favoritesRepository,
                    achievementRepository = appContainer.achievementRepository
                ) as T
            }
        }
    }
}
