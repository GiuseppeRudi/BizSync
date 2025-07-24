package com.bizsync.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import java.time.LocalDate
import java.time.LocalTime

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

            // Timeline oraria con slot
            TimelineConSlots(
                orarioInizio = orari.first,
                orarioFine = orari.second,
                turniAssegnati = turniAssegnati
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun TimelineConSlots(
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    turniAssegnati: List<Turno>
) {
    val inizioMinuti = orarioInizio.hour * 60 + orarioInizio.minute
    val fineMinuti = orarioFine.hour * 60 + orarioFine.minute
    val slotDurata = 60 // 1 ora per slot

    val slots = (inizioMinuti until fineMinuti step slotDurata).map { minuti ->
        val ore = minuti / 60
        val min = minuti % 60
        String.format("%02d:%02d", ore, min)
    }

    Column {
        // Header orari
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = orarioInizio.toString().take(5), // "HH:mm"
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = orarioFine.toString().take(5), // "HH:mm"
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Slots timeline
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(slots) { slot ->
                SlotOrario(
                    orario = slot,
                    isCoperto = isSlotCoperto(slot, turniAssegnati),
                    hasSovrapposto = hasOverlap(slot, turniAssegnati)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legenda
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendaItem("Coperto", MaterialTheme.colorScheme.primary)
            LegendaItem("Scoperto", MaterialTheme.colorScheme.outline)
            LegendaItem("Sovrapposto", MaterialTheme.colorScheme.error)
        }
    }
}

// Funzioni utility aggiornate per confrontare LocalTime invece che stringhe

private fun isSlotCoperto(slot: String, turni: List<Turno>): Boolean {
    val slotTime = LocalTime.parse(slot)
    return turni.any { turno ->
        // slot è coperto se è >= orarioInizio e < orarioFine
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




@Composable
private fun SlotOrario(
    orario: String,
    isCoperto: Boolean,
    hasSovrapposto: Boolean
) {
    val color = when {
        hasSovrapposto -> MaterialTheme.colorScheme.error
        isCoperto -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(20.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = orario.take(5), // HH:MM
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendaItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

