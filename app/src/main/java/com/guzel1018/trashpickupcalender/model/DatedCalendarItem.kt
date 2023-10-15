package com.guzel1018.trashpickupcalender.model

import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.concurrent.Immutable
import java.time.LocalDate

@Immutable
data class DatedCalendarItem(
    val date: LocalDate,
    val gs: String?,
    val kind: String,
    val p: String?,
    val rm: String?,
    val townId:String
)