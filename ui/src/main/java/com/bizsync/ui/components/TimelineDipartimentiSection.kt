package com.bizsync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.bizsync.domain.model.AreaLavoro
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun TimelineDipartimentiSection(
    dipartimenti: List<AreaLavoro>,
    giornoSelezionato: LocalDate
) {
    val dayOfWeek = giornoSelezionato.dayOfWeek

    // Filtra dipartimenti che hanno orari per questo giorno e ordinali per orario
    val dipartimentiDelGiorno = dipartimenti
        .filter { it.orariSettimanali.containsKey(dayOfWeek) }
        .sortedBy {
            it.orariSettimanali[dayOfWeek]?.first?.replace(":", "")?.toIntOrNull() ?: 0
        }

    if (dipartimentiDelGiorno.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nessun dipartimento aperto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = getNomeGiorno(dayOfWeek),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Header con giorno selezionato
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Orari ${getNomeGiorno(dayOfWeek)} ${giornoSelezionato.format(DateTimeFormatter.ofPattern("dd/MM"))}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline orizzontale
        TimelineOrizzontale(dipartimentiDelGiorno, dayOfWeek)

        Spacer(modifier = Modifier.height(16.dp))

        // Lista dipartimenti con dettagli
        dipartimentiDelGiorno.forEach { dipartimento ->
            DipartimentoOrarioCard(
                dipartimento = dipartimento,
                dayOfWeek = dayOfWeek
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TimelineOrizzontale(
    dipartimenti: List<AreaLavoro>,
    dayOfWeek: DayOfWeek
) {
    val orariEstesi = dipartimenti.mapNotNull { dipartimento ->
        dipartimento.orariSettimanali[dayOfWeek]?.let { (inizio, fine) ->
            Triple(dipartimento.nomeArea, inizio, fine)
        }
    }

    if (orariEstesi.isEmpty()) return

    // Calcola range orario totale
    val orarioMinimo = orariEstesi.minOfOrNull { it.second.replace(":", "").toInt() } ?: 800
    val orarioMassimo = orariEstesi.maxOfOrNull { it.third.replace(":", "").toInt() } ?: 1800

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Timeline Giornata",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline grafica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                // Linea base timeline
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.outline
                )

                // Orari di inizio e fine giornata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimeMarker(formatOrario(orarioMinimo), true)
                    TimeMarker(formatOrario(orarioMassimo), false)
                }

                // Barre dipartimenti
                orariEstesi.forEachIndexed { index, (nome, inizio, fine) ->
                    val inizioNum = inizio.replace(":", "").toInt()
                    val fineNum = fine.replace(":", "").toInt()

                    val startPercent = ((inizioNum - orarioMinimo).toFloat() / (orarioMassimo - orarioMinimo))
                    val widthPercent = ((fineNum - inizioNum).toFloat() / (orarioMassimo - orarioMinimo))

                    DipartimentoTimelineBar(
                        nome = nome,
                        startPercent = startPercent,
                        widthPercent = widthPercent,
                        index = index,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun DipartimentoTimelineBar(
    nome: String,
    startPercent: Float,
    widthPercent: Float,
    index: Int,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )

    val barHeight = 12.dp
    val verticalOffset = (index * 16.dp) - 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = verticalOffset)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(widthPercent)
                .height(barHeight)
                .offset(x = (startPercent * LocalConfiguration.current.screenWidthDp.dp)),
            colors = CardDefaults.cardColors(
                containerColor = colors[index % colors.size]
            )
        ) {
            // Contenuto vuoto, solo la barra colorata
        }
    }
}

@Composable
private fun TimeMarker(orario: String, isStart: Boolean) {
    Column(
        horizontalAlignment = if (isStart) Alignment.Start else Alignment.End
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = orario,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun formatOrario(orarioNumerico: Int): String {
    val ore = orarioNumerico / 100
    val minuti = orarioNumerico % 100
    return String.format("%02d:%02d", ore, minuti)
}