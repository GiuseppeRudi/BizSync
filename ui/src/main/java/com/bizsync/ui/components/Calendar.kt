package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.ui.viewmodels.CalendarViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// QUESTO CALENDAR LO VEDIAMO PIU TARDI

@Composable
fun Calendar() {
    val currentDate = remember { LocalDate.now() } // Giorno corrente
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    val calendarviewmodel : CalendarViewModel = viewModel()


    Column(
        modifier = Modifier
            .background(Color.White),
    ) {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
        )

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            WeekCalendar(
                modifier = Modifier.padding(vertical = 4.dp),
                state = state,
                calendarScrollPaged = false,
                dayContent = { day ->
                    Day(
                        date = day.date,
                        isCurrentDay = day.date == currentDate,
                        selected = calendarviewmodel.selectionData.value == day.date,
                    ) {
                        calendarviewmodel.selectionData.value = it
                    }
                },
            )
        }
    }

}

private val dateFormatter = DateTimeFormatter.ofPattern("dd")


@Composable
fun Day(
    date: LocalDate,
    isCurrentDay: Boolean = false,
    selected: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // **Definizione del bordo**
    val borderColor = when {
        selected -> Color(0xFFBB86FC) // Viola per il giorno selezionato
        isCurrentDay -> Color(0xFF002DFF) // Azzurro per il giorno corrente
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .width(screenWidth / 9)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF6200EE) else Color(0xFF1E88E5)) // Blu normale, viola se selezionato
            .border(
                shape = RoundedCornerShape(8.dp),
                width = if (borderColor != Color.Transparent) 2.dp else 0.dp, // Mostra il bordo solo se necessario
                color = borderColor
            )
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.month.name.take(3), // Abbreviazione mese
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = date.dayOfWeek.name.take(3), // Abbreviazione giorno
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
            )

        }
    }
}

@Preview
@Composable
private fun CalendarPreview() {
    Calendar()
}
