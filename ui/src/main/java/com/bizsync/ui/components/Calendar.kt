package com.bizsync.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.bizsync.domain.constants.enumClass.WeeklyShiftStatus
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.ui.viewmodels.PianificaViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.items

@Composable
fun Calendar(pianificaVM: PianificaViewModel) {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(100) }
    val endDate = remember { currentDate.plusDays(100) }

    val pianificaState by pianificaVM.uistate.collectAsState()
    val selectionData = pianificaState.selectionData
    val weeklyShift = pianificaState.weeklyShiftRiferimento
    var rangeSettimana by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var weeklyShiftStatus by remember { mutableStateOf<WeeklyShiftStatus?>(null) }

    // 🆕 Stato del calendario con gestione dinamica della data iniziale
    val initialVisibleDate = selectionData ?: currentDate
    val state = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = initialVisibleDate,
    )

    // 🆕 Auto-scroll quando selectionData cambia
    LaunchedEffect(selectionData) {
        val targetDate = selectionData ?: currentDate

        Log.d("Calendar", "Auto-scroll verso: $targetDate (selectionData: $selectionData)")

        try {
            // Anima verso la settimana che contiene la data target
            state.animateScrollToWeek(targetDate)
        } catch (e: Exception) {
            Log.w("Calendar", "Errore durante l'animazione scroll: ${e.message}")
            // Fallback: scroll senza animazione
            state.scrollToWeek(targetDate)
        }
    }

    LaunchedEffect(weeklyShift) {
        rangeSettimana = if (weeklyShift != null) {
            WeeklyWindowCalculator.getWeekBounds(weeklyShift.weekStart)
        } else {
            null
        }
        weeklyShiftStatus = weeklyShift?.status
        Log.d("Calendar", "Range settimana aggiornato: $rangeSettimana con status: $weeklyShiftStatus")
    }

    Column(
        modifier = Modifier.background(Color.White),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            WeekCalendar(
                modifier = Modifier.padding(vertical = 4.dp),
                state = state,
                calendarScrollPaged = false,
                dayContent = { day ->
                    Day(
                        date = day.date,
                        isCurrentDay = day.date == currentDate,
                        selected = selectionData == day.date,
                        isInWeekRange = rangeSettimana?.let { range ->
                            day.date >= range.first && day.date <= range.second
                        } ?: false,
                        weeklyShiftStatus = if (rangeSettimana?.let { range ->
                                day.date >= range.first && day.date <= range.second
                            } == true) weeklyShiftStatus else null,
                        onClick = { pianificaVM.onSelectionDataChanged(it) }
                    )
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
    isInWeekRange: Boolean = false,
    weeklyShiftStatus: WeeklyShiftStatus? = null,
    onClick: (LocalDate) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val backgroundColor = when {
        selected -> Color(0xFF6200EE)
        isInWeekRange && weeklyShiftStatus != null -> {
            when (weeklyShiftStatus) {
                WeeklyShiftStatus.PUBLISHED -> Color(0xFF4CAF50)     // Verde - Pubblicata
                WeeklyShiftStatus.DRAFT -> Color(0xFFFFC107)         // Giallo - Bozza
                WeeklyShiftStatus.NOT_PUBLISHED -> Color(0xFFF44336) // Rosso - Non pubblicata
            }
        }
        isInWeekRange -> Color(0xFF4CAF50)      // Verde di default se non c'è status
        else -> Color(0xFF1E88E5)               // Blu normale - Default
    }

    val borderColor = when {
        selected -> Color(0xFFBB86FC)           // Bordo viola per selezionato
        isCurrentDay -> Color(0xFF002DFF)       // Bordo blu scuro per oggi
        isInWeekRange && weeklyShiftStatus != null -> {
            when (weeklyShiftStatus) {
                WeeklyShiftStatus.PUBLISHED -> Color(0xFF2E7D32)     // Verde scuro
                WeeklyShiftStatus.DRAFT -> Color(0xFFFF8F00)         // Arancione scuro
                WeeklyShiftStatus.NOT_PUBLISHED -> Color(0xFFD32F2F) // Rosso scuro
            }
        }
        isInWeekRange -> Color(0xFF2E7D32)      // Verde scuro di default
        else -> Color.Transparent
    }

    val textColor = when {
        selected || isInWeekRange -> Color.White
        else -> Color.White
    }

    val dayTextColor = when {
        selected -> Color.White
        isInWeekRange && weeklyShiftStatus != null -> {
            when (weeklyShiftStatus) {
                WeeklyShiftStatus.PUBLISHED -> Color(0xFFE8F5E8)
                WeeklyShiftStatus.DRAFT -> Color(0xFFFFF3E0)
                WeeklyShiftStatus.NOT_PUBLISHED -> Color(0xFFFFEBEE)
            }
        }
        isInWeekRange -> Color(0xFFE8F5E8)
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .width(screenWidth / 9)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                shape = RoundedCornerShape(8.dp),
                width = if (borderColor != Color.Transparent) 2.dp else 0.dp,
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
                text = date.month.name.take(3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Text(
                text = date.dayOfWeek.name.take(3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = dayTextColor,
            )

            if (isInWeekRange && !selected) {
                val indicatorColor = when (weeklyShiftStatus) {
                    WeeklyShiftStatus.PUBLISHED -> Color.White
                    WeeklyShiftStatus.DRAFT -> Color(0xFF333333)
                    WeeklyShiftStatus.NOT_PUBLISHED -> Color.White
                    null -> Color.White
                }

                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(indicatorColor, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = Color(0xFF6200EE),
                    text = "Selezionato",
                    modifier = Modifier.weight(1f)
                )

                LegendItem(
                    color = Color(0xFF4CAF50),
                    text = "Pubblicata",
                    modifier = Modifier.weight(1f)
                )

                LegendItem(
                    color = Color(0xFFFFC107),
                    text = "Bozza",
                    modifier = Modifier.weight(1f)
                )

                LegendItem(
                    color = Color(0xFFF44336),
                    text = "Non pubblicata",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun DayPreviewGrid() {
    val today = LocalDate.now()
    val examples = listOf(
        "Giorno normale:" to DayProps(today, isCurrentDay = false, selected = false, isInWeekRange = false),
        "Settimana PUBBLICATA:" to DayProps(today.plusDays(1), isCurrentDay = false, selected = false, isInWeekRange = true, weeklyShiftStatus = WeeklyShiftStatus.PUBLISHED),
        "Giorno corrente:" to DayProps(today, isCurrentDay = true, selected = false, isInWeekRange = false),
        "Settimana BOZZA:" to DayProps(today.plusDays(2), isCurrentDay = false, selected = false, isInWeekRange = true, weeklyShiftStatus = WeeklyShiftStatus.DRAFT),
        "Giorno selezionato:" to DayProps(today.plusDays(4), isCurrentDay = false, selected = true, isInWeekRange = true, weeklyShiftStatus = WeeklyShiftStatus.PUBLISHED),
        "Settimana NON PUBBLICATA:" to DayProps(today.plusDays(3), isCurrentDay = false, selected = false, isInWeekRange = true, weeklyShiftStatus = WeeklyShiftStatus.NOT_PUBLISHED)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(examples) { (label, props) ->
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Day(
                    date = props.date,
                    isCurrentDay = props.isCurrentDay,
                    selected = props.selected,
                    isInWeekRange = props.isInWeekRange,
                    weeklyShiftStatus = props.weeklyShiftStatus
                )
            }
        }
    }
}

private data class DayProps(
    val date: LocalDate,
    val isCurrentDay: Boolean,
    val selected: Boolean,
    val isInWeekRange: Boolean,
    val weeklyShiftStatus: WeeklyShiftStatus = WeeklyShiftStatus.PUBLISHED
)
