package com.guzel1018.trashpickupcalender.model

import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val gs: String?,
    val name: String,
    val p: String?,
    val rm: String?
)