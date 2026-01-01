package com.guzel1018.trashpickupcalender.utils

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.CalendarItem
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.model.Town
import com.guzel1018.trashpickupcalender.utils.DataTransformations.getCalendarItems
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(InternalSerializationApi::class)
object DataTransformations {

    private fun getEvents(): List<CalendarItem> {
        val resourceAsStream =
            DataTransformations::class.java.classLoader.getResourceAsStream("data/calendar.json")
        val resourceAsString = IOUtils.toString(resourceAsStream, Charsets.UTF_8)
        return Json.decodeFromString(resourceAsString)
    }

    fun getCalendarItems(): List<DatedCalendarItem> {
        return getEvents().map {
            DatedCalendarItem(
                gs = it.gs,
                kind = it.kind,
                p = it.p,
                rm = it.rm,
                date = LocalDate.parse(it.date, DateTimeFormatter.BASIC_ISO_DATE),
                townId = it.town_id
            )
        }
    }
}

@OptIn(InternalSerializationApi::class)
fun getTowns(): List<Town> {
    val resourceAsStream =
        DataTransformations::class.java.classLoader.getResourceAsStream("data/towns.json")
    val resourceAsString = IOUtils.toString(resourceAsStream, Charsets.UTF_8)
    return Json.decodeFromString(resourceAsString)
}

@OptIn(InternalSerializationApi::class)
fun getRegions(town:Town) : List<Region> {
   val selectedTown = getTowns().first {
       it == town
   }
    return selectedTown.regions
}

@OptIn(InternalSerializationApi::class)
fun getEventsByTown(town:Town): List<DatedCalendarItem> {
    return getCalendarItems().filter { it.townId == town.town_id }
}

@OptIn(InternalSerializationApi::class)
fun getEventsByTownAndRegion(town:Town, region: Region) : List<DatedCalendarItem> {
   return getEventsByTown(town)
        .filter { item ->
            // Determine which waste type we're dealing with based on which field is non-null
            when {
                item.rm != null -> item.rm == region.rm  // Restmüll: filter by rm only
                item.p != null -> item.p == region.p      // Papier: filter by p only
                item.gs != null -> item.gs == region.gs   // Gelber Sack: filter by gs only
                else -> true  // If all are null (e.g., Bio, Gelbe Tonne), include for all regions
            }
        }
}

@OptIn(InternalSerializationApi::class)
fun getTownFromUserData(userAddress: UserAddress): Town {
    return getTowns().first { it.name == userAddress.townName }
}

@OptIn(InternalSerializationApi::class)
fun getRegionFromUserData(userAddress: UserAddress): Region {
    return getTowns().first { it.name == userAddress.townName }.regions.first {
        it.name == userAddress.streetName
    }
}



