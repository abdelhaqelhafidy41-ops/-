package com.example.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.AppContainer
import com.example.data.local.entity.IslamicLibraryEntity
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.LibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IslamicLibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _selectedBookId = MutableStateFlow("ALL")
    val selectedBookId: StateFlow<String> = _selectedBookId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val libraryItems: StateFlow<List<IslamicLibraryEntity>> = combine(_searchQuery, _selectedBookId) { query, bookId ->
        query to bookId
    }.flatMapLatest { (query, bookId) ->
        if (query.isNotBlank()) {
            libraryRepository.searchLibrary(query)
        } else if (bookId == "ALL") {
            libraryRepository.getAllItems()
        } else {
            libraryRepository.getItemsByBook(bookId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteIds: StateFlow<Set<String>> = favoritesRepository.getFavoritesByType("LIBRARY")
        .map { list -> list.map { it.itemId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun selectBook(bookId: String) {
        _selectedBookId.value = bookId
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(item: IslamicLibraryEntity) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(
                itemType = "LIBRARY",
                itemId = item.id.toString(),
                title = item.chapterName,
                subtitle = "${item.bookName} • ${item.reference}",
                arabicText = item.textArabic,
                reference = item.reference
            )
        }
    }

    companion object {
        val BOOKS = listOf(
            "ALL" to "جميع الكتب",
            "BUKHARI" to "صحيح البخاري",
            "MUSLIM" to "صحيح مسلم",
            "NAWAWI_40" to "الأربعون النووية",
            "RIYAD_SALIHIN" to "رياض الصالحين",
            "HISN_ALMUSLIM" to "حصن المسلم",
            "SEERAH" to "السيرة النبوية"
        )

        fun Factory(appContainer: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IslamicLibraryViewModel(
                    libraryRepository = appContainer.libraryRepository,
                    favoritesRepository = appContainer.favoritesRepository
                ) as T
            }
        }
    }
}
