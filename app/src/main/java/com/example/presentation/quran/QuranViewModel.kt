package com.example.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.QuranAudioPlayer
import com.example.core.util.AppContainer
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.KhatmahEntity
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.entity.MemorizationQuizResultEntity
import com.example.data.local.entity.QuranSurahEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.KhatmahRepository
import com.example.data.repository.MemorizationRepository
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(
    private val quranRepository: QuranRepository,
    private val favoritesRepository: FavoritesRepository,
    private val khatmahRepository: KhatmahRepository,
    private val memorizationRepository: MemorizationRepository,
    val audioPlayer: QuranAudioPlayer
) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Ensure 114 surahs and initial seed data are populated in Room
        viewModelScope.launch {
            quranRepository.ensureInitialData()
        }
    }

    val surahs: StateFlow<List<QuranSurahEntity>> = _searchQuery
        .flatMapLatest { query ->
            quranRepository.searchSurahs(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val readingProgress: StateFlow<ReadingProgressEntity?> = quranRepository.getReadingProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val favoriteAyahs: StateFlow<List<FavoriteEntity>> = favoritesRepository.getFavoritesByType("QURAN_AYAH")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val khatmahs: StateFlow<List<KhatmahEntity>> = khatmahRepository.getAllKhatmahs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val memorizationRecords: StateFlow<List<MemorizationEntity>> = memorizationRepository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quizResults: StateFlow<List<MemorizationQuizResultEntity>> = memorizationRepository.getRecentQuizResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearBookmark() {
        viewModelScope.launch {
            quranRepository.clearReadingProgress()
        }
    }

    // Khatmah operations
    fun createKhatmah(title: String, targetDays: Int, targetType: String, totalTarget: Int) {
        viewModelScope.launch {
            khatmahRepository.createKhatmah(title, targetDays, targetType, totalTarget)
        }
    }

    fun recordKhatmahProgress(khatmahId: Long, amount: Int) {
        viewModelScope.launch {
            khatmahRepository.recordProgress(khatmahId, amount)
        }
    }

    fun togglePauseKhatmah(khatmahId: Long, isPaused: Boolean) {
        viewModelScope.launch {
            khatmahRepository.togglePauseKhatmah(khatmahId, !isPaused)
        }
    }

    fun deleteKhatmah(khatmahId: Long) {
        viewModelScope.launch {
            khatmahRepository.deleteKhatmah(khatmahId)
        }
    }

    // Memorization operations
    fun addMemorizationRecord(
        surahNumber: Int,
        surahName: String,
        startAyah: Int,
        endAyah: Int,
        masteryLevel: Int,
        nextReviewDays: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            memorizationRepository.addOrUpdateRecord(
                surahNumber, surahName, startAyah, endAyah, masteryLevel, nextReviewDays, notes
            )
        }
    }

    fun logMemorizationReview(recordId: Long, newMasteryLevel: Int, nextReviewDays: Int) {
        viewModelScope.launch {
            memorizationRepository.logReview(recordId, newMasteryLevel, nextReviewDays)
        }
    }

    fun deleteMemorizationRecord(recordId: Long) {
        viewModelScope.launch {
            memorizationRepository.deleteRecord(recordId)
        }
    }

    fun saveQuizResult(surahNumber: Int, surahName: String, totalQuestions: Int, correctAnswers: Int) {
        viewModelScope.launch {
            memorizationRepository.saveQuizResult(surahNumber, surahName, totalQuestions, correctAnswers)
        }
    }

    suspend fun getAyahsForSurah(surahNumber: Int): List<com.example.data.local.entity.QuranAyahEntity> {
        return try {
            quranRepository.downloadSurahAyahs(surahNumber)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuranViewModel(
                    quranRepository = appContainer.quranRepository,
                    favoritesRepository = appContainer.favoritesRepository,
                    khatmahRepository = appContainer.khatmahRepository,
                    memorizationRepository = appContainer.memorizationRepository,
                    audioPlayer = appContainer.audioPlayer
                ) as T
            }
        }
    }
}
