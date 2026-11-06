package com.example.presentation.wallpapers

import android.app.WallpaperManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.importer.WallpaperDataImporter
import com.example.data.local.entity.WallpaperEntity
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WallpaperCategoryItem(
    val id: String,
    val labelArabic: String
)

data class WallpapersUiState(
    val wallpapers: List<WallpaperEntity> = emptyList(),
    val selectedCategory: String = "ALL",
    val searchQuery: String = "",
    val selectedWallpaper: WallpaperEntity? = null,
    val isApplying: Boolean = false,
    val applySuccessMessage: String? = null,
    val errorMessage: String? = null
)

class WallpapersViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val wallpaperDataImporter: WallpaperDataImporter
) : ViewModel() {

    val categories = listOf(
        WallpaperCategoryItem("ALL", "الكل"),
        WallpaperCategoryItem("FAVORITES", "المفضلة ❤️"),
        WallpaperCategoryItem("DOWNLOADED", "المحملة 💾"),
        WallpaperCategoryItem("mosques", "المساجد 🕌"),
        WallpaperCategoryItem("kaaba", "الكعبة المشرفة 🕋"),
        WallpaperCategoryItem("prophet_mosque", "المسجد النبوي 🟢"),
        WallpaperCategoryItem("calligraphy", "الخط العربي ✍️"),
        WallpaperCategoryItem("geometry", "الزخارف 💠"),
        WallpaperCategoryItem("quran_verses", "آيات قرآنية 📖"),
        WallpaperCategoryItem("adhkar", "أذكار 📿"),
        WallpaperCategoryItem("ramadan", "رمضان 🌙"),
        WallpaperCategoryItem("eid", "العيد 🎉"),
        WallpaperCategoryItem("sky_stars", "السماء والنجوم ✨"),
        WallpaperCategoryItem("nature", "الطبيعة والصحراء 🏜️"),
        WallpaperCategoryItem("sea_sunset", "البحر والغروب 🌅"),
        WallpaperCategoryItem("dark", "داكنة 🌑"),
        WallpaperCategoryItem("light", "فاتحة ☀️"),
        WallpaperCategoryItem("lock_screen", "شاشة القفل 🔒"),
        WallpaperCategoryItem("home_screen", "الرئيسية 📱"),
        WallpaperCategoryItem("minimal", "بسيطة هادئة 🌿"),
        WallpaperCategoryItem("tablet", "للأجهزة اللوحية 💻")
    )

    private val _selectedCategory = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _selectedWallpaper = MutableStateFlow<WallpaperEntity?>(null)
    private val _isApplying = MutableStateFlow(false)
    private val _applySuccessMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private data class DialogState(
        val selected: WallpaperEntity? = null,
        val isApplying: Boolean = false,
        val successMsg: String? = null,
        val errorMsg: String? = null
    )

    private val _dialogState = combine(
        _selectedWallpaper,
        _isApplying,
        _applySuccessMessage,
        _errorMessage
    ) { selected, isApplying, successMsg, errorMsg ->
        DialogState(selected, isApplying, successMsg, errorMsg)
    }

    val uiState: StateFlow<WallpapersUiState> = combine(
        wallpaperRepository.getAllWallpapers(),
        _selectedCategory,
        _searchQuery,
        _dialogState
    ) { allWps, category, query, dialog ->
        val filtered = when {
            category == "FAVORITES" -> allWps.filter { it.isFavorite }
            category == "DOWNLOADED" -> allWps.filter { it.isDownloaded }
            category != "ALL" -> allWps.filter { it.category == category }
            else -> allWps
        }.let { list ->
            if (query.isBlank()) list
            else list.filter { wp ->
                wp.titleArabic.contains(query, ignoreCase = true) ||
                (wp.titleEnglish != null && wp.titleEnglish.contains(query, ignoreCase = true)) ||
                wp.designerOrAuthor.contains(query, ignoreCase = true)
            }
        }

        WallpapersUiState(
            wallpapers = filtered,
            selectedCategory = category,
            searchQuery = query,
            selectedWallpaper = dialog.selected,
            isApplying = dialog.isApplying,
            applySuccessMessage = dialog.successMsg,
            errorMessage = dialog.errorMsg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WallpapersUiState())

    init {
        viewModelScope.launch {
            wallpaperDataImporter.seedWallpapersIfNeeded()
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectWallpaper(wallpaper: WallpaperEntity?) {
        _selectedWallpaper.value = wallpaper
    }

    fun toggleFavorite(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            val newFav = !wallpaper.isFavorite
            wallpaperRepository.toggleFavorite(wallpaper.id, newFav)
            if (_selectedWallpaper.value?.id == wallpaper.id) {
                _selectedWallpaper.value = _selectedWallpaper.value?.copy(isFavorite = newFav)
            }
        }
    }

    fun downloadWallpaper(context: Context, wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            val result = wallpaperRepository.downloadWallpaper(wallpaper, context)
            if (result.isSuccess) {
                _applySuccessMessage.value = "تم تنزيل الخلفية بنجاح في جهازك"
                if (_selectedWallpaper.value?.id == wallpaper.id) {
                    _selectedWallpaper.value = _selectedWallpaper.value?.copy(
                        isDownloaded = true,
                        localFilePath = result.getOrNull()
                    )
                }
            } else {
                _errorMessage.value = "تعذر تنزيل الخلفية، تحقق من الاتصال بالإنترنت"
            }
        }
    }

    fun deleteLocalCopy(context: Context, wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            wallpaperRepository.deleteLocalCopy(wallpaper, context)
            if (_selectedWallpaper.value?.id == wallpaper.id) {
                _selectedWallpaper.value = _selectedWallpaper.value?.copy(
                    isDownloaded = false,
                    localFilePath = null
                )
            }
            _applySuccessMessage.value = "تم حذف النسخة المحملة محلياً"
        }
    }

    fun applyWallpaper(context: Context, wallpaper: WallpaperEntity, target: Int) {
        viewModelScope.launch {
            _isApplying.value = true
            val result = wallpaperRepository.applyWallpaper(context, wallpaper, target)
            _isApplying.value = false
            if (result.isSuccess) {
                val targetText = when (target) {
                    WallpaperManager.FLAG_SYSTEM -> "الشاشة الرئيسية"
                    WallpaperManager.FLAG_LOCK -> "شاشة القفل"
                    else -> "الشاشة الرئيسية وشاشة القفل"
                }
                _applySuccessMessage.value = "تم تعيين الخلفية لـ $targetText بنجاح ✨"
            } else {
                _errorMessage.value = "تعذر تطبيق الخلفية: ${result.exceptionOrNull()?.message ?: "خطأ غير متوقع"}"
            }
        }
    }

    fun clearFeedback() {
        _applySuccessMessage.value = null
        _errorMessage.value = null
    }

    companion object {
        fun provideFactory(
            wallpaperRepository: WallpaperRepository,
            wallpaperDataImporter: WallpaperDataImporter
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WallpapersViewModel(wallpaperRepository, wallpaperDataImporter) as T
            }
        }
    }
}
