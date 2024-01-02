package com.guzel1018.trashpickupcalender.utils

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.CalendarItem
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.model.Town
import com.guzel1018.trashpickupcalender.utils.DataTransformations.getCalendarItems
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

fun getTowns(): List<Town> {
    val resourceAsStream =
        DataTransformations::class.java.classLoader.getResourceAsStream("data/towns.json")
    val resourceAsString = IOUtils.toString(resourceAsStream, Charsets.UTF_8)
    return Json.decodeFromString(resourceAsString)
}

fun getRegions(town:Town) : List<Region> {
   val selectedTown = getTowns().first {
       it == town
   }
    return selectedTown.regions
}

fun getEventsByTown(town:Town): List<DatedCalendarItem> {
    return getCalendarItems().filter { it.townId == town.town_id }
}

fun getEventsByTownAndRegion(town:Town, region: Region) : List<DatedCalendarItem> {
   return getEventsByTown(town)
        .filter { it.rm == region.rm && it.p == region.p && it.gs == region.gs}
        .map {
            DatedCalendarItem(
                gs = it.gs,
                kind = it.kind,
                p = it.p,
                rm = it.rm,
                date = it.date,
                townId = it.townId
            )
        }
}

fun getTownFromUserData(userAddress: UserAddress): Town {
    return getTowns().first { it.name == userAddress.townName }
}

fun getRegionFromUserData(userAddress: UserAddress): Region {
    return getTowns().first { it.name == userAddress.townName }.regions.first {
        it.name == userAddress.streetName
    }
}



