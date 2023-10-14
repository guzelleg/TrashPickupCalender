package com.guzel1018.trashpickupcalender.utils

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils
import com.guzel1018.trashpickupcalender.model.CalendarItem
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DataTransformations {

    fun getEvents(): List<CalendarItem> {
        val resourceAsStream =
            DataTransformations::class.java.classLoader.getResourceAsStream("data/calendar_hbg_2023.json")
        val resourceAsString = IOUtils.toString(resourceAsStream, Charsets.UTF_8)
        return Json.decodeFromString(resourceAsString)
    }

    fun transform(): List<DatedCalendarItem> {
      return getEvents().map {
            DatedCalendarItem(
                gs = it.gs,
                kind = it.kind,
                p = it.p,
                rm = it.rm,
                date = LocalDate.parse(it.date, DateTimeFormatter.BASIC_ISO_DATE)
            )
        }
    }
}



