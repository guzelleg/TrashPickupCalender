package com.guzel1018.trashpickupcalender.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Region(
    val gs: String?,
    val name: String,
    val p: String?,
    val rm: String?
)