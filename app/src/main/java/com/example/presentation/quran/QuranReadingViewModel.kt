package com.example.presentation.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.QuranAudioPlayer
import com.example.audio.Reciter
import com.example.audio.RepeatMode
import com.example.core.util.AppContainer
import com.example.data.local.entity.QuranAyahEntity
import com.example.data.local.entity.QuranSurahEntity
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranReadingUiState(
    val surah: QuranSurahEntity? = null,
    val ayahs: List<QuranAyahEntity> = emptyList(),
    val filteredAyahs: List<QuranAyahEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val fontSizeSp: Float = 24f,
    val fontType: String = "AMIRI", // "AMIRI", "CAIRO", "SYSTEM"
    val backgroundTheme: String = "WHITE", // "WHITE", "SEPIA", "DARK", "GREEN"
    val selectedAyahForActions: QuranAyahEntity? = null,
    val isFavoriteSelected: Boolean = false,
    val isAudioDownloading: Boolean = false,
    val audioDownloadProgress: Pair<Int, Int>? = null
)

class QuranReadingViewModel(
    val surahNumber: Int,
    private val quranRepository: QuranRepository,
    private val favoritesRepository: FavoritesRepository,
    val audioPlayer: QuranAudioPlayer,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranReadingUiState())
    val uiState: StateFlow<QuranReadingUiState> = _uiState.asStateFlow()

    init {
        loadSurahData()
    }

    fun loadSurahData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val surah = quranRepository.getSurah(surahNumber)
            _uiState.update { it.copy(surah = surah) }

            // Observe ayahs from Room
            quranRepository.getAyahsForSurah(surahNumber).collect { ayahsList ->
                if (ayahsList.isNotEmpty() && (surah == null || ayahsList.size >= surah.totalAyahs)) {
                    _uiState.update {
                        it.copy(
                            ayahs = ayahsList,
                            filteredAyahs = filterAyahs(ayahsList, it.searchQuery),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    // Auto save reading position for first/current ayah
                    surah?.let { s ->
                        quranRepository.saveReadingProgress(
                            surahNumber = s.surahNumber,
                            surahName = s.nameArabic,
                            ayahNumber = ayahsList.first().ayahNumberInSurah,
                            page = s.startPage,
                            juz = 1
                        )
                    }
                } else {
                    // Try downloading from official verified API
                    downloadFromVerifiedApi()
                }
            }
        }
    }

    fun retryDownload() {
        viewModelScope.launch {
            downloadFromVerifiedApi()
        }
    }

    private suspend fun downloadFromVerifiedApi() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val fetched = quranRepository.downloadSurahAyahs(surahNumber)
            _uiState.update {
                it.copy(
                    ayahs = fetched,
                    filteredAyahs = filterAyahs(fetched, it.searchQuery),
                    isLoading = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "تعذر تحميل آيات السورة، يرجى إعادة المحاولة."
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredAyahs = filterAyahs(it.ayahs, query)
            )
        }
    }

    private fun filterAyahs(list: List<QuranAyahEntity>, query: String): List<QuranAyahEntity> {
        if (query.isBlank()) return list
        return list.filter {
            it.textUthmani.contains(query.trim(), ignoreCase = true) ||
                    it.ayahNumberInSurah.toString() == query.trim()
        }
    }

    fun setFontSize(size: Float) {
        _uiState.update { it.copy(fontSizeSp = size.coerceIn(16f, 44f)) }
    }

    fun setFontType(type: String) {
        _uiState.update { it.copy(fontType = type) }
    }

    fun setBackgroundTheme(theme: String) {
        _uiState.update { it.copy(backgroundTheme = theme) }
    }

    fun selectAyahForActions(ayah: QuranAyahEntity?) {
        viewModelScope.launch {
            val isFav = if (ayah != null) {
                favoritesRepository.isFavorite("QURAN_AYAH", "${ayah.surahNumber}:${ayah.ayahNumberInSurah}").firstOrNull() ?: false
            } else false

            _uiState.update {
                it.copy(selectedAyahForActions = ayah, isFavoriteSelected = isFav)
            }
        }
    }

    fun toggleFavoriteSelectedAyah() {
        val ayah = _uiState.value.selectedAyahForActions ?: return
        val surah = _uiState.value.surah ?: return
        viewModelScope.launch {
            val newFavState = favoritesRepository.toggleFavorite(
                itemType = "QURAN_AYAH",
                itemId = "${ayah.surahNumber}:${ayah.ayahNumberInSurah}",
                title = "سورة ${surah.nameArabic} - آية ${ayah.ayahNumberInSurah}",
                subtitle = "الجزء ${ayah.juzNumber} • الصفحة ${ayah.pageNumber}",
                arabicText = ayah.textUthmani,
                reference = "${ayah.surahNumber}:${ayah.ayahNumberInSurah}"
            )
            _uiState.update { it.copy(isFavoriteSelected = newFavState) }
        }
    }

    fun saveBookmarkForAyah(ayah: QuranAyahEntity) {
        val surah = _uiState.value.surah ?: return
        viewModelScope.launch {
            quranRepository.saveReadingProgress(
                surahNumber = surah.surahNumber,
                surahName = surah.nameArabic,
                ayahNumber = ayah.ayahNumberInSurah,
                page = ayah.pageNumber,
                juz = ayah.juzNumber
            )
        }
    }

    fun playAyah(ayahNumber: Int) {
        val surah = _uiState.value.surah ?: return
        audioPlayer.playAyah(
            surahNumber = surah.surahNumber,
            ayahNumber = ayahNumber,
            surahName = surah.nameArabic,
            totalAyahs = surah.totalAyahs
        )
    }

    fun downloadSurahAudio() {
        val surah = _uiState.value.surah ?: return
        val reciter = audioPlayer.state.value.reciter
        viewModelScope.launch {
            _uiState.update { it.copy(isAudioDownloading = true) }
            val success = audioPlayer.downloadSurahAudio(
                surahNumber = surah.surahNumber,
                totalAyahs = surah.totalAyahs,
                reciter = reciter,
                onProgress = { current, total ->
                    _uiState.update { it.copy(audioDownloadProgress = Pair(current, total)) }
                }
            )
            _uiState.update { it.copy(isAudioDownloading = false, audioDownloadProgress = null) }
        }
    }

    fun copyAyahText(ayah: QuranAyahEntity) {
        val surah = _uiState.value.surah ?: return
        val formatted = "﴿ ${ayah.textUthmani} ﴾ [سورة ${surah.nameArabic}: ${ayah.ayahNumberInSurah}]"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("آية قرآنية", formatted))
    }

    fun shareAyahText(ayah: QuranAyahEntity) {
        val surah = _uiState.value.surah ?: return
        val formatted = "﴿ ${ayah.textUthmani} ﴾\n[سورة ${surah.nameArabic} - الآية ${ayah.ayahNumberInSurah}]\n— من تطبيق نور المسلم"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formatted)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الآية الكريمة").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    companion object {
        fun Factory(surahNumber: Int, appContainer: AppContainer, context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuranReadingViewModel(
                        surahNumber = surahNumber,
                        quranRepository = appContainer.quranRepository,
                        favoritesRepository = appContainer.favoritesRepository,
                        audioPlayer = appContainer.audioPlayer,
                        context = context
                    ) as T
                }
            }
    }
}
