package com.guzel1018.trashpickupcalender.model

import kotlinx.serialization.Serializable

@Serializable
data class CalendarItem(
    val date: String,
    val gs: String?,
    val kind: String,
    val p: String?,
    val rm: String?
)