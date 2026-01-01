package com.guzel1018.trashpickupcalender.ui

import androidx.lifecycle.ViewModel
import kotlinx.serialization.InternalSerializationApi
import androidx.lifecycle.viewModelScope
import com.guzel1018.trashpickupcalender.model.Town
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
import com.guzel1018.trashpickupcalender.utils.getEventsByTown
import com.guzel1018.trashpickupcalender.utils.getEventsByTownAndRegion
import com.guzel1018.trashpickupcalender.utils.getRegionFromUserData
import com.guzel1018.trashpickupcalender.utils.getRegions
import com.guzel1018.trashpickupcalender.utils.getTownFromUserData
import com.guzel1018.trashpickupcalender.utils.getTowns
import com.kizitonwose.calendar.core.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(InternalSerializationApi::class)
@HiltViewModel
class MainViewModel @Inject constructor (
    private val addressService: AddressService,
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchStreetText = MutableStateFlow("")
    val searchStreetText = _searchStreetText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _savedAddress = MutableStateFlow(UserAddress())
    var savedAddress: StateFlow<UserAddress> = _savedAddress

    private val _selectedDay: MutableStateFlow<CalendarDay?> = MutableStateFlow(null)
    val selectedDay = _selectedDay.asStateFlow()

    fun setSelectedDay(day: CalendarDay?) {
        _selectedDay.value = day
    }

    private val _towns = MutableStateFlow(getTowns())
    @OptIn(FlowPreview::class)
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


    private val _regions = MutableStateFlow(_searchUiState.value.currentSelectedTown?.let {
        getRegions(it)
    } ?: listOf())

    @OptIn(FlowPreview::class)
    val regions = searchStreetText
        .debounce(500L)
        .combine(_regions) { text, regions ->
            regions.filter {
                it.name.contains(text, ignoreCase = true)
            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            _regions.value
        )


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
        viewModelScope.launch {
            addressService.addAddress(town = selectedTown, null)
            setSavedAddress()
        }
    }

    fun setRegions(selectedTown: Town) {
        _regions.update { getRegions(selectedTown) }
    }

    fun setEvents(events: List<DatedCalendarItem>?) {
        _events.update {
            events
        }
    }

    fun setSelectedRegion(selectedRegion: Region) {
        _searchUiState.update { currentState ->
            currentState.copy(currentSelectedStreet = selectedRegion)
        }
        viewModelScope.launch {
            addressService.addAddress(
                town = searchUiState.value.currentSelectedTown!!,
                selectedRegion
            )
            setSavedAddress()
        }
    }

    private suspend fun setSavedAddress() {
        _savedAddress.value = addressService.getAddressFromDataStore().first()
    }

    fun deleteSavedData() =
        viewModelScope.launch {
            addressService.deleteAddress()
            setSavedAddress()
        }

    init {
        viewModelScope.launch {
            initializeSavedAddress()
            initializeSearchUiState()
            initializeEvents()
        }
    }

    private suspend fun initializeSavedAddress() {
        _isLoading.value = true
        _savedAddress.update {
            addressService.getAddressFromDataStore().first()
        }
        _isLoading.value = false
    }

    private fun initializeSearchUiState() {
        val userTown = getTownFromSavedAddress()
        val userStreet = getRegionFromSavedAddress()

        _searchUiState.update { currentState ->
            currentState.copy(
                currentSelectedStreet = userStreet,
                currentSelectedTown = userTown
            )
        }
    }

    private fun initializeEvents() {
        val userTown = getTownFromSavedAddress()
        val userStreet = getRegionFromSavedAddress()

        if (userStreet == null) {
            setEvents(userTown?.let { getEventsByTown(it) })
        } else {
            setEvents(userTown?.let { getEventsByTownAndRegion(it, userStreet) })
        }
    }

    private fun getTownFromSavedAddress(): Town? {
        return if (_savedAddress.value.townName.isNullOrBlank()) null
        else getTownFromUserData(_savedAddress.value)
    }

    private fun getRegionFromSavedAddress(): Region? {
        return if (_savedAddress.value.streetName.isNullOrBlank()) null
        else getRegionFromUserData(_savedAddress.value)
    }
}