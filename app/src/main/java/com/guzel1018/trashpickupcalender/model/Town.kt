package com.guzel1018.trashpickupcalender.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Town(
    val name: String,
    val regions: List<Region>,
    val town_id: String
){
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            name.lowercase()
        )
        return matchingCombinations.any {it.contains(query, ignoreCase = true)}
    }
}