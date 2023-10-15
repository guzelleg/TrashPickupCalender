package com.guzel1018.trashpickupcalender.utils

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.guzel1018.trashpickupcalender.model.CalendarItem
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
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

fun getHainburgEvents(): List<DatedCalendarItem> {
    return getCalendarItems().filter { it.townId == "30710" }
}

fun getHainburgEventsPerRegion(
    filterRm: String,
    filterP: String,
    filterGs: String
): List<DatedCalendarItem> {
    return getHainburgEvents()
        .filter { it.rm == filterRm && it.p == filterP && it.gs == filterGs}
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



