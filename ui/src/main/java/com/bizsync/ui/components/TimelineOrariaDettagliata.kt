package com.bizsync.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

@Composable
fun TimelineOrariaDettagliata(
    dipartimento: AreaLavoro,
    giornoSelezionato: LocalDate,
    turniAssegnati: List<Turno>
) {
    val dayOfWeek = giornoSelezionato.dayOfWeek
    val orari = dipartimento.orariSettimanali[dayOfWeek] ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Copertura Oraria",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline oraria responsive
            TimelineResponsive(
                orarioInizio = orari.first,
                orarioFine = orari.second,
                turniAssegnati = turniAssegnati
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun TimelineResponsive(
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    turniAssegnati: List<Turno>
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val inizioMinuti = orarioInizio.hour * 60 + orarioInizio.minute
    val fineMinuti = orarioFine.hour * 60 + orarioFine.minute
    val slotDurata = 60 // 1 ora per slot

    val slots = (inizioMinuti until fineMinuti step slotDurata).map { minuti ->
        val ore = minuti / 60
        val min = minuti % 60
        String.format("%02d:%02d", ore, min)
    }

    // Calcola dimensioni responsive
    val totalSlots = slots.size
    val availableWidth = screenWidth - 64.dp // Padding della card
    val maxSlotWidth = 40.dp
    val minSlotWidth = 20.dp

    // Calcola la larghezza ottimale per slot
    val idealSlotWidth = availableWidth / totalSlots
    val slotWidth = idealSlotWidth.coerceIn(minSlotWidth, maxSlotWidth)

    // Determina se mostrare tutti gli orari o solo alcuni
    val showAllLabels = slotWidth >= 28.dp
    val showReducedLabels = slotWidth >= 24.dp && !showAllLabels

    Column {
        // Header orari con etichette agli estremi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = orarioInizio.toString().take(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = orarioFine.toString().take(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timeline responsive senza scroll
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            slots.forEachIndexed { index, slot ->
                SlotOrarioResponsive(
                    orario = slot,
                    isCoperto = isSlotCoperto(slot, turniAssegnati),
                    hasSovrapposto = hasOverlap(slot, turniAssegnati),
                    slotWidth = slotWidth,
                    showLabel = when {
                        showAllLabels -> true
                        showReducedLabels -> index % 2 == 0 // Mostra ogni 2 ore
                        else -> index % 4 == 0 // Mostra ogni 4 ore
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legenda responsive
        ResponsiveLegenda(slotWidth)
    }
}

@Composable
private fun SlotOrarioResponsive(
    orario: String,
    isCoperto: Boolean,
    hasSovrapposto: Boolean,
    slotWidth: androidx.compose.ui.unit.Dp,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        hasSovrapposto -> MaterialTheme.colorScheme.error
        isCoperto -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    // Determina altezza del box basata sulla larghezza
    val boxHeight = min(slotWidth.value * 0.6f, 24f).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
                .background(color, RoundedCornerShape(2.dp))
        )

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = orario.take(5),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = when {
                        slotWidth >= 32.dp -> 9.sp
                        slotWidth >= 24.dp -> 8.sp
                        else -> 7.sp
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp)) // Spazio per allineamento
        }
    }
}

@Composable
private fun ResponsiveLegenda(slotWidth: androidx.compose.ui.unit.Dp) {
    val isCompact = slotWidth < 28.dp

    if (isCompact) {
        // Legenda compatta verticale
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LegendaItem("Coperto", MaterialTheme.colorScheme.primary, isCompact = true)
            LegendaItem("Scoperto", MaterialTheme.colorScheme.outline, isCompact = true)
            LegendaItem("Sovrapposto", MaterialTheme.colorScheme.error, isCompact = true)
        }
    } else {
        // Legenda normale orizzontale
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LegendaItem("Coperto", MaterialTheme.colorScheme.primary)
            LegendaItem("Scoperto", MaterialTheme.colorScheme.outline)
            LegendaItem("Sovrapposto", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun LegendaItem(
    label: String,
    color: Color,
    isCompact: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 8.dp else 12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = if (isCompact) {
                MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            } else {
                MaterialTheme.typography.labelSmall
            }
        )
    }
}

// Versione alternativa con slot più intelligenti per timeline molto lunghe
@Composable
private fun TimelineAdaptive(
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    turniAssegnati: List<Turno>
) {
    val inizioMinuti = orarioInizio.hour * 60 + orarioInizio.minute
    val fineMinuti = orarioFine.hour * 60 + orarioFine.minute

    // Determina la granularità ottimale basata sulla durata
    val durataMinuti = fineMinuti - inizioMinuti
    val slotDurata = when {
        durataMinuti <= 480 -> 60 // <= 8 ore: slot da 1 ora
        durataMinuti <= 720 -> 90 // <= 12 ore: slot da 1.5 ore
        else -> 120 // > 12 ore: slot da 2 ore
    }

    val slots = (inizioMinuti until fineMinuti step slotDurata).map { minuti ->
        val ore = minuti / 60
        val min = minuti % 60
        String.format("%02d:%02d", ore, min) to slotDurata
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = orarioInizio.toString().take(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = orarioFine.toString().take(5),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            slots.forEach { (slot, duration) ->
                SlotOrarioAdaptive(
                    orario = slot,
                    duration = duration,
                    isCoperto = isSlotCopertoDuration(slot, duration, turniAssegnati),
                    hasSovrapposto = hasOverlapDuration(slot, duration, turniAssegnati),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendaItem("Coperto", MaterialTheme.colorScheme.primary)
            LegendaItem("Scoperto", MaterialTheme.colorScheme.outline)
            LegendaItem("Sovrapposto", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SlotOrarioAdaptive(
    orario: String,
    duration: Int,
    isCoperto: Boolean,
    hasSovrapposto: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        hasSovrapposto -> MaterialTheme.colorScheme.error
        isCoperto -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = orario.take(5),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (duration > 60) {
            Text(
                text = "${duration/60}h",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Funzioni utility rimangono identiche
private fun isSlotCoperto(slot: String, turni: List<Turno>): Boolean {
    val slotTime = LocalTime.parse(slot)
    return turni.any { turno ->
        slotTime >= turno.orarioInizio && slotTime < turno.orarioFine
    }
}

private fun hasOverlap(slot: String, turni: List<Turno>): Boolean {
    val slotTime = LocalTime.parse(slot)
    val count = turni.count { turno ->
        slotTime >= turno.orarioInizio && slotTime < turno.orarioFine
    }
    return count > 1
}

private fun isSlotCopertoDuration(slot: String, duration: Int, turni: List<Turno>): Boolean {
    val slotTime = LocalTime.parse(slot)
    val slotEnd = slotTime.plusMinutes(duration.toLong())
    return turni.any { turno ->
        // Slot coperto se c'è sovrapposizione con almeno un turno
        !(turno.orarioFine <= slotTime || turno.orarioInizio >= slotEnd)
    }
}

private fun hasOverlapDuration(slot: String, duration: Int, turni: List<Turno>): Boolean {
    val slotTime = LocalTime.parse(slot)
    val slotEnd = slotTime.plusMinutes(duration.toLong())
    val overlappingTurni = turni.filter { turno ->
        !(turno.orarioFine <= slotTime || turno.orarioInizio >= slotEnd)
    }
    return overlappingTurni.size > 1
}