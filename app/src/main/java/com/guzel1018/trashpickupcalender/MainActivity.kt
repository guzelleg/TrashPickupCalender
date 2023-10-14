package com.guzel1018.trashpickupcalender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guzel1018.trashpickupcalender.ui.theme.TrashPickupCalenderTheme
import com.guzel1018.trashpickupcalender.utils.DataTransformations.transform
import com.guzel1018.trashpickupcalender.utils.displayText
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        transform()
        super.onCreate(savedInstanceState)
        setContent {
            TrashPickupCalenderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                   Column {
                       MainScreen()
                   }
                }
            }
        }
    }
}


@Composable
fun MainScreen(adjacentMonths: Long = 500) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(adjacentMonths) }
    val endMonth = remember { currentMonth.plusMonths(adjacentMonths) }
    val selections = remember { mutableStateListOf<CalendarDay>() }
    val daysOfWeek = remember { daysOfWeek() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = daysOfWeek.first(),
        )
        val coroutineScope = rememberCoroutineScope()
        val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)

        SimpleCalendarTitle(
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
        )
        HorizontalCalendar(
            modifier = Modifier.testTag("Calendar"),
            state = state,
            dayContent = { day ->
                Day(day, isSelected = selections.contains(day)) { clicked ->
                    if (selections.contains(clicked)) {
                        selections.remove(clicked)
                    } else {
                        selections.add(clicked)
                    }
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
private fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .testTag("MonthDay")
            .padding(6.dp)
            .clip(CircleShape)
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
        Column {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 20.sp,
            )
            Text(text = "Papier", fontSize = 12.sp, color = textColor,)
        }
    }
}

@Composable
fun TrashTypeChips(
    chips: ImmutableList<ChipData>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: ChipData, index: Int) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(start = 5.dp, end = 5.dp),
        contentPadding = PaddingValues(10.dp),
    )
    {
        itemsIndexed(chips) { index, item -> itemContent(item, index) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chip(data: ChipData,
         isSelected: Boolean,
         onClick: () -> Unit,
         ) {
    Surface(
        color = if (isSelected) Color.Transparent else data.color,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.DarkGray,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 6.dp,
                horizontal = 10.dp,
            ),
        ) {
            Text(
                text = data.text
            )
        }
    }

}

@Composable
fun DetailsCard(text: String) {
    Card (
        shape = RoundedCornerShape(2.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ){
        Text(text = text, fontSize = 34.sp )
        Text(text = "All Available dates:", fontSize = 25.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Column {
            Text(text = "30.05", fontSize = 20.sp)
            Text(text = "30.08", fontSize = 20.sp)
            Text(text = "30.09", fontSize = 20.sp)
        }

    }

}

data class ChipData (
    val text: String,
    val contentDescription: String = text,
    val color: Color
)

data class CalendarEvent(
    val event: TrashKind,
    val calendarDay: CalendarDay
)

enum class TrashKind (val trashName: String){
    BIO("Bio"),
    GELBE_TONNE("Gelbe Tonne"),
    PAPER_2WEEKS ("Papier 2-w\u00f6chig"),
    PAPER_4WEEKS("Papier 4-w\u00f6chig"),
    PAPIER_8WEEKS("Papier 8-w\u00f6chig"),
    RESTMUELL_HALF_YEAR( "Restm\u00fcll halbj\u00e4hrig"),
    RESTMUELL_4WEEKS("Restm\u00fcll 4-w\u00f6chig"),
    GELBE_SACK("Gelber Sack")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrashPickupCalenderTheme {
        MainScreen()
    }
}