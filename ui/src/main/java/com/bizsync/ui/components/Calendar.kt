package com.bizsync.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
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
import com.bizsync.domain.utils.WeeklyWindowCalculator
import com.bizsync.ui.viewmodels.PianificaViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Calendar(pianificaVM: PianificaViewModel) {
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }

    val pianificaState by pianificaVM.uistate.collectAsState()
    val selectionData = pianificaState.selectionData
    val weeklyShift = pianificaState.weeklyShiftRiferimento

    // ✅ Stato per il range della settimana di riferimento
    var rangeSettimana by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }

    LaunchedEffect(weeklyShift) {
        rangeSettimana = if (weeklyShift != null) {
            WeeklyWindowCalculator.getWeekBounds(weeklyShift.weekStart)
        } else {
            null
        }
        Log.d("Calendar", "Range settimana aggiornato: $rangeSettimana")
    }

    Column(
        modifier = Modifier.background(Color.White),
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
                        selected = selectionData == day.date,
                        isInWeekRange = rangeSettimana?.let { range ->
                            day.date >= range.first && day.date <= range.second
                        } ?: false, // ✅ Nuovo parametro per identificare i giorni nella settimana
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
    isInWeekRange: Boolean = false, // ✅ Nuovo parametro
    onClick: (LocalDate) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // ✅ Logica colori aggiornata con priorità
    val backgroundColor = when {
        selected -> Color(0xFF6200EE)           // Viola - Massima priorità (giorno selezionato)
        isInWeekRange -> Color(0xFF4CAF50)      // Verde - Giorni della settimana di riferimento
        else -> Color(0xFF1E88E5)               // Blu normale - Default
    }

    val borderColor = when {
        selected -> Color(0xFFBB86FC)           // Bordo viola per selezionato
        isCurrentDay -> Color(0xFF002DFF)       // Bordo blu scuro per oggi
        isInWeekRange -> Color(0xFF2E7D32)      // Bordo verde scuro per settimana
        else -> Color.Transparent
    }

    // ✅ Colore del testo adattato al background
    val textColor = when {
        selected || isInWeekRange -> Color.White
        else -> Color.White
    }

    val dayTextColor = when {
        selected -> Color.White
        isInWeekRange -> Color(0xFFE8F5E8)     // Verde molto chiaro per i giorni della settimana
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

            // ✅ Indicatore visivo aggiuntivo per la settimana di riferimento
            if (isInWeekRange && !selected) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

// ✅ Estensione utility per WeeklyWindowCalculator se non esiste già
object AbsenceWindowCalculatorExtension {

    /**
     * Calcola i bounds di una settimana (da lunedì a domenica)
     */
    fun getWeekBounds(weekStart: LocalDate): Pair<LocalDate, LocalDate> {
        val lunedi = weekStart.with(java.time.DayOfWeek.MONDAY)
        val domenica = lunedi.plusDays(6)
        return Pair(lunedi, domenica)
    }
}

// ✅ Preview per testare i diversi stati
@Preview(showBackground = true)
@Composable
fun DayPreview() {
    val today = LocalDate.now()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Giorno normale:")
        Day(
            date = today,
            isCurrentDay = false,
            selected = false,
            isInWeekRange = false
        )

        Text("Giorno corrente:")
        Day(
            date = today,
            isCurrentDay = true,
            selected = false,
            isInWeekRange = false
        )

        Text("Giorno nella settimana di riferimento:")
        Day(
            date = today.plusDays(1),
            isCurrentDay = false,
            selected = false,
            isInWeekRange = true
        )

        Text("Giorno selezionato nella settimana:")
        Day(
            date = today.plusDays(2),
            isCurrentDay = false,
            selected = true,
            isInWeekRange = true
        )
    }
}