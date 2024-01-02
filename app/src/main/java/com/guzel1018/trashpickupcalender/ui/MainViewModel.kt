package com.guzel1018.trashpickupcalender.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.guzel1018.trashpickupcalender.model.Town
import com.guzel1018.trashpickupcalender.utils.getTowns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.service.AddressService
import com.guzel1018.trashpickupcalender.utils.getRegions
import com.kizitonwose.calendar.core.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val addressService: AddressService
): ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchStreetText = MutableStateFlow("")
    val searchStreetText = _searchStreetText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    val savedAddress = addressService.getAddressFromDataStore().asLiveData()

    private val _selectedDay: MutableStateFlow<CalendarDay?> = MutableStateFlow(null)
    val selectedDay = _selectedDay.asStateFlow()

    fun setSelectedDay(day:CalendarDay?){
        _selectedDay.value = day
    }

    val _towns = MutableStateFlow(getTowns())
    val towns = searchText
        .debounce(500L)
        .combine(_towns) { text, towns ->
        if (text.isBlank()) {
            towns
        } else towns.filter { it.doesMatchSearchQuery(text) }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        _towns.value
    )

    private val _events = MutableStateFlow<List<DatedCalendarItem>?>(listOf())
    val events = _events.asStateFlow()


    private val _regions = MutableStateFlow( _searchUiState.value.currentSelectedTown?.let {
        getRegions(it)
    } ?: listOf())

    val regions = searchStreetText
        .debounce(500L)
        .combine(_regions){
            text, regions ->
            regions.filter {
                it.name.contains(text, ignoreCase = true)
            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            _regions.value
        )

    fun saveAddressData(address: UserAddress) {
        viewModelScope.launch {
            Log.d("Address", "Data was inserted")
                    addressService.addAddress(address)
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onStreetSearchTextChange(text: String) {
        _searchStreetText.value = text
    }

    fun clearSearchText() {
        _searchText.value = ""
    }

    fun clearStreetSearchText() {
        _searchStreetText.value = ""
    }

    fun setSelectedTown(selectedTown: Town) {
        _searchUiState.update { currentState ->
            currentState.copy(currentSelectedTown = selectedTown)
        }
    }

    fun setRegions(selectedTown: Town) {
        _regions.update { getRegions(selectedTown)}
    }

    fun setEvents(events: List<DatedCalendarItem>?){
        _events.update {
            events
        }
    }

    fun setSelectedRegion(selectedRegion: Region) {
        _searchUiState.update { currentState ->
            currentState.copy(currentSelectedStreet = selectedRegion)
        }
    }
}