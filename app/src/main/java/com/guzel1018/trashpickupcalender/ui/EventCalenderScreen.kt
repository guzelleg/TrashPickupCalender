package com.guzel1018.trashpickupcalender.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.guzel1018.trashpickupcalender.R
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.rememberFirstMostVisibleMonth
import com.guzel1018.trashpickupcalender.utils.displayText
import com.kizitonwose.calendar.compose.ContentHeightMode
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth


@SuppressLint("StateFlowValueCalledInComposition", "CoroutineCreationDuringComposition")
@Composable
fun EventCalenderScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
    ) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY) }
    val events by viewModel.events.collectAsState()

    val selectedCalendarDay by viewModel.selectedDay.collectAsState()
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails && selectedCalendarDay !=null) {
        AlertDialog(
            onDismissRequest = { },
            dismissButton = {
                Button(onClick = {
                    showDetails = false
                }) {
                    Text(text = "Schließen")
                } },
            confirmButton = { },
            text = {
                Column {
                    Text(text = "${selectedCalendarDay!!.date.month} ${selectedCalendarDay!!.date.dayOfMonth},  ${selectedCalendarDay!!.date.year}",
                        fontSize = 16.sp)
                    Text(text = "")
                    for (event in events?.groupBy { it.date }?.get(selectedCalendarDay!!.date)?.distinctBy { it.kind }!!)
                        Text(text = event.kind)
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val coroutineScope = rememberCoroutineScope()
        val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)

        CalendarScreen(
            modifier = Modifier.padding( horizontal = 8.dp),
            currentMonth = visibleMonth.yearMonth,
            goToPrevious = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                }
            },
            goToNext = {
                coroutineScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                }
            },
            viewModel = viewModel,
            navController = navController
        )
        HorizontalCalendar(
            modifier = Modifier.testTag("Calendar"),
            contentHeightMode = ContentHeightMode.Wrap,
            state = state,
            dayContent = { day ->
                Day(
                    day,
                    events = events?.groupBy { it.date }?.get(day.date)?.distinctBy { it.kind }
                )
                {
                    viewModel.setSelectedDay(it)
                    showDetails = true
                }
            },
            monthHeader = {
                MonthHeader(daysOfWeek = daysOfWeek)
            },
        )
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("MonthHeader"),
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                text = dayOfWeek.displayText(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    events: List<DatedCalendarItem>?,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.55f) // This is important for square-sizing!
            .testTag("MonthDay")
            .background(color = Color.Transparent)
            .clickable(
                onClick = { onClick(day) }),
        contentAlignment = Alignment.TopCenter,
    ) {
        val textColor = when (day.position) {
            DayPosition.MonthDate -> Color.Unspecified
            DayPosition.InDate, DayPosition.OutDate -> colorResource(R.color.inactive_text_color)
        }
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 22.sp,
            )
            Column {
                if (events != null) {
                    for (event in events) {
                        Text(
                            text = getNameAbbreviation(event.kind),
                            fontSize = 14.sp,
                            color = textColor,
                            modifier = Modifier.background(getBackgroundColor(event.kind))
                        )
                    }
                }
            }
        }
    }
}

fun getNameAbbreviation(fullName: String): String {
    return when (fullName) {
        "Bio", "Bioabfall, Wilfleinsdorf", "Bioabfall, Gebiet A", "Bioabfall, Gebiet B" -> "bio"
        "Gelbe Tonne" -> "GbT"
        "Papier 2-wöchig" -> "P2W"
        "Papier 4-wöchig" -> "P4W"
        "Papier 8-wöchig", "Papier 8-w\u00f6chig, Gebiet A", "Papier 8-w\u00f6chig, Gebiet B" -> "P8W"
        "Restmüll halbjährig" -> "RHb"
        "Restm\u00fcll 4-w\u00f6chig in Zone Margarethen am Moos","Restmüll 4-wöchig", "Restm\u00fcll 4-w\u00f6chig in Zone Enzersdorf" -> "R4W"
        "Restm\u00fcll 2-w\u00f6chig halbj\u00e4hrig, Wilfleinsdorf" -> "R2W"
        "Restm\u00fcll 2-w\u00f6chig", "Restm\u00fcll 2-w\u00f6chig halbj\u00e4hrig", "Restm\u00fcll 2-w\u00f6chig, Wilfleinsdorf" -> "R2W"
        "Restm\u00fcll 1-w\u00f6chig" -> "R1W"
        "Papier 8-w\u00f6chig in Zone Margarethen am Moos", "Restm\u00fcll 8-w\u00f6chig und halbj\u00e4hrig, Wilfleinsdorf" -> "R8W"
        "Restm\u00fcll, Gebiet A" -> "RA"
        "Restm\u00fcll, Gebiet B" -> "RB"
        "Restm\u00fcll, Gebiet C" -> "RC"
        "Gelber Sack", "Gelber Sack, Gebiet C", "Gelber Sack, Gebiet A", "Gelber Sack, Gebiet B" -> "GS"
        else -> fullName
    }
}

fun getBackgroundColor(name: String): Color {
    return when (name) {
        "Bio" -> Color.Green
        "Gelbe Tonne", "Gelber Sack" -> Color.Yellow
        "Papier 2-wöchig", "Papier 4-wöchig", "Papier 8-wöchig" -> Color.Red
        "Restm\u00fcll 4-w\u00f6chig" -> Color.Gray
        else -> Color.White
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CalendarScreen(
    modifier: Modifier,
    currentMonth: YearMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
    viewModel: MainViewModel,
    navController: NavHostController,
) {

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = { showDialog = false
                        navController.navigate(FilterScreen.Start.name)
                        viewModel.deleteSavedData()
                    }) {
                    Text(text = "Ja, ändern")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false

                }) {
                    Text(text = "Nein, hier bleiben")
                }
            },
            text =  {Text(text = "Gewählte Gemeinde ändern?")},
        )
    }

    var showInfo by remember { mutableStateOf(false) }

    if (showInfo) {
        AlertDialog(onDismissRequest = {}, confirmButton = {},
            text = { Column{
                Text(text = "Abkürzungen:")
                Text(text = "")
                Text(text = "bio - Biomüll", Modifier.background(Color.Green))
                Text(text = "GbT - Gelbe Tonne", Modifier.background(Color.Yellow))
                Text(text = "GS - Gelber Sack", Modifier.background(Color.Yellow))
                Text(text = "P2W - Papier 2-wöchig", Modifier.background(Color.Red))
                Text(text = "P4W - Papier 4-wöchig", Modifier.background(Color.Red))
                Text(text = "P8W - Papier 8-wöchig",Modifier.background(Color.Red))
                Text(text = "RHb -Restmüll halbjährig", Modifier.background(Color.Gray))
                Text(text = "R4W - Restmüll 4-wöchig", Modifier.background(Color.Gray))
            }},
            dismissButton = {
                Button(onClick = { showInfo = false }) {
                    Text(text = "Schließen")
                }
            },
                )
    }

    Column {
        viewModel.savedAddress.let {
            Text(
                text = "${it.value.townName}",
                fontSize = 25.sp, modifier = Modifier.padding(start = 10.dp, top = 10.dp)
            )
            if (it.value.streetName != "") {Text(
                text = "${viewModel.savedAddress.value.streetName}",
                fontSize = 18.sp, modifier = Modifier.padding(start = 10.dp)
            )}
        }

        Row(horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { showDialog = true },

                ) {
                Text(text = "Andere Gemeinde auswählen")
            }

            TextButton(
                onClick = { showInfo = true },

                ) {
                Text(text = "Info")
            }
        }
    }

    Row(
        modifier = modifier.height(30.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("MonthTitle")
                .padding(bottom = 5.dp),
            text = currentMonth.displayText(),
            fontSize = 22.sp,
            textAlign = TextAlign.Start,
        )
        CalendarNavigationIcon(
            icon = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "Previous",
            onClick = goToPrevious,
        )
        CalendarNavigationIcon(
            icon = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = "Next",
            onClick = goToNext,
        )
    }
}


@Composable
private fun CalendarNavigationIcon(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .clip(shape = CircleShape)
        .clickable(role = Role.Button, onClick = onClick),
) {
    Icon(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center),
        painter = icon,
        contentDescription = contentDescription,
    )
}