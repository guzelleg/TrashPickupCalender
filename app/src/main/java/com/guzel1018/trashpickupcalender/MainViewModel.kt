package com.guzel1018.trashpickupcalender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guzel1018.trashpickupcalender.model.Town
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _towns = MutableStateFlow(listOf<Town>())
    val towns = searchText.combine(_towns) { text, towns ->
        if (text.isBlank()) {
            towns
        } else towns.filter { it.doesMatchSearchQuery(text) }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        _towns.value
    )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}