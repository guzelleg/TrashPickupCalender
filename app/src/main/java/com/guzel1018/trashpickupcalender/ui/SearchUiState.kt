package com.guzel1018.trashpickupcalender.ui

import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.model.Town

data class SearchUiState(
    val currentSelectedTown: Town? = null,
    val currentSelectedStreet: Region? = null
)
