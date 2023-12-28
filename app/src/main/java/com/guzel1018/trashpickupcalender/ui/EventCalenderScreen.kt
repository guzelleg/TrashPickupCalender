package com.guzel1018.trashpickupcalender.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guzel1018.trashpickupcalender.R
import com.guzel1018.trashpickupcalender.clickable
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.rememberFirstMostVisibleMonth
import com.guzel1018.trashpickupcalender.ui.theme.TrashPickupCalenderTheme
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


@Composable
fun EventCalenderScreen(
    viewModel: MainViewModel,
    searchDetailUiState: SearchUiState) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }
    val events by viewModel.events.collectAsState()

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
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
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
            searchDetailUiState = searchDetailUiState,
        )
        HorizontalCalendar(
            modifier = Modifier.testTag("Calendar"),
            contentHeightMode = ContentHeightMode.Fill,
            state = state,
            dayContent = { day ->
                Day(
                    day,
                    isSelected = selections.contains(day),
                    events = events?.groupBy { it.date }?.get(day.date)
                )
                {}
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
    isSelected: Boolean,
    events: List<DatedCalendarItem>?,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .testTag("MonthDay")
            .padding(6.dp)
            .background(color = if (isSelected) colorResource(R.color.example_1_selection_color) else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                showRipple = !isSelected,
                onClick = { onClick(day) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val textColor = when (day.position) {
            // Color.Unspecified will use the default text color from the current theme
            DayPosition.MonthDate -> if (isSelected) Color.White else Color.Unspecified
            DayPosition.InDate, DayPosition.OutDate -> colorResource(R.color.inactive_text_color)
        }
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 20.sp,
            )
            Column {
                if (events != null) {
                    for (event in events) {
                        Text(
                            text = getNameAbbreviation(event.kind),
                            fontSize = 18.sp,
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
        "Bio" -> "bio"
        "Gelbe Tonne" -> "GbT"
        "Papier 2-wöchig" -> "P2W"
        "Papier 4-wöchig" -> "P4W"
        "Papier 8-wöchig" -> "P8W"
        "Restmüll halbjährig" -> "RHb"
        "Restmüll 4-wöchig" -> "R4W"
        "Gelber Sack" -> "GS"
        else -> ""
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

@Composable
fun CalendarScreen(
    modifier: Modifier,
    currentMonth: YearMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
    searchDetailUiState: SearchUiState
) {

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }) {
                    Text(text = "Yes, reset my selection")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(text = "No, stay here")
                }
            },
            text =  {Text(text = "Are you sure you want to select another town?")},
        )
    }
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        searchDetailUiState.currentSelectedTown?.let {
            Text(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f),
                text = it.name,
                fontSize = 25.sp,
            )
        }
        Text(text = "Select another town",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier
                .padding(end = 5.dp)
                .clickable(role = Role.Button, onClick = { showDialog = true }),)
    }

    Row(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("MonthTitle")
                .padding(start = 13.dp),
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrashPickupCalenderTheme {
    }
}