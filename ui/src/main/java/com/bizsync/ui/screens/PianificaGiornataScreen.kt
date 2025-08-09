package com.bizsync.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.bizsync.domain.constants.enumClass.DipartimentoStatus
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.components.EmptyDayCard
import com.bizsync.ui.components.GiornoHeaderCard
import com.bizsync.ui.components.SectionHeader
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


@Composable
fun PianificaGiornata(
    dipartimenti: List<AreaLavoro>,
    giornoSelezionato: LocalDate,
    managerVM : PianificaManagerViewModel,
    onDipartimentoClick: (AreaLavoro) -> Unit,
) {

    val managerState by managerVM.uiState.collectAsState()
    val dayOfWeek = giornoSelezionato.dayOfWeek
    val turniCaricamento = managerState.isLoadingTurni
    val turniGiorno = managerState.turniGiornalieri

    val dipartimentiDelGiorno = dipartimenti
        .filter { it.orariSettimanali.containsKey(dayOfWeek) }
        .sortedBy { it.orariSettimanali[dayOfWeek]?.first ?: LocalTime.MIN }


    LaunchedEffect(giornoSelezionato,turniCaricamento) {
        managerVM.setLoading(true)

        if(!turniCaricamento)
        {
            managerVM.setTurniGiornalieri(dayOfWeek, dipartimentiDelGiorno)

        }
    }

    val turniSettimanali = managerState.turniSettimanali
    Log.d("PianificaGiornata", "selectionDate: $giornoSelezionato")
    Log.d("PianificaGiornata", "dipartimentiDelGiorno: $dipartimentiDelGiorno")
    Log.d("PianificaGiornata", "turniDelGiorno: $turniGiorno")
    Log.d("PianificaGiornata", "turniSettimanali: $turniSettimanali")



    if (managerState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }


    if (dipartimentiDelGiorno.isEmpty()) {
        EmptyDayCard(dayOfWeek)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GiornoHeaderCard(giornoSelezionato, dayOfWeek)
        }

        item {
            SectionHeader(
                title = "Dipartimenti da Pianificare",
                subtitle = "${dipartimentiDelGiorno.size} aperti oggi"
            )
        }

        // Lista dipartimenti con stato
        items(dipartimentiDelGiorno) { dipartimento ->

            if (dipartimento.nomeArea in turniGiorno) {
                DipartimentoStatoCard(
                    dipartimento = dipartimento,
                    dayOfWeek = dayOfWeek,
                    onClick = { onDipartimentoClick(dipartimento)
                                managerVM.setTurniGiornalieriDipartimento(dipartimento.nomeArea)},
                    turniDipartimentoGiornalieri = turniGiorno[dipartimento.nomeArea]!!
                )
            }

        }

    }

}



@Composable
private fun DipartimentoStatoCard(
    dipartimento: AreaLavoro,
    dayOfWeek: DayOfWeek,
    onClick: () -> Unit,
    turniDipartimentoGiornalieri: List<Turno>
) {
    val orari = dipartimento.orariSettimanali[dayOfWeek] ?: return

    // Calcola tutti i valori basandosi sui turni del giorno
    val risultatiAnalisi = calcolaStatoDipartimento(dipartimento, turniDipartimentoGiornalieri, orari)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (risultatiAnalisi.stato) {
                DipartimentoStatus.COMPLETE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = dipartimento.nomeArea,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${orari.first} - ${orari.second}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (risultatiAnalisi.buchi.isNotEmpty()) {
                        Text(
                            text = "${risultatiAnalisi.buchi.size} ${if (risultatiAnalisi.buchi.size == 1) "buco orario" else "buchi orari"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (risultatiAnalisi.sovrapposizioni.isNotEmpty()) {
                        Text(
                            text = "${risultatiAnalisi.sovrapposizioni.size} ${if (risultatiAnalisi.sovrapposizioni.size == 1) "sovrapposizione" else "sovrapposizioni"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (risultatiAnalisi.turniAssegnati > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${risultatiAnalisi.turniAssegnati}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Progress indicator
                CircularProgressIndicator(
                    progress = { risultatiAnalisi.percentualeCopertura.coerceIn(0f, 1f) },
                    modifier = Modifier.size(24.dp),
                    color = when (risultatiAnalisi.stato) {
                        DipartimentoStatus.COMPLETE -> MaterialTheme.colorScheme.primary
                        DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                        DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.outline
                    },
                    strokeWidth = 3.dp,
                )

                // Status icon
                Icon(
                    imageVector = when (risultatiAnalisi.stato) {
                        DipartimentoStatus.COMPLETE -> Icons.Default.CheckCircle
                        DipartimentoStatus.PARTIAL -> Icons.Default.Warning
                        DipartimentoStatus.INCOMPLETE -> Icons.Default.Circle
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when (risultatiAnalisi.stato) {
                        DipartimentoStatus.COMPLETE -> MaterialTheme.colorScheme.primary
                        DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                        DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.outline
                    }
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Apri",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * Risultato dell'analisi di un dipartimento
 */
data class RisultatoAnalisiDipartimento(
    val stato: DipartimentoStatus,
    val turniAssegnati: Int,
    val oreCoperte: Double,
    val oreTotali: Double,
    val percentualeCopertura: Float,
    val buchi: List<BucoOrario>,
    val sovrapposizioni: List<SovrapposizioneOrari>
)

data class BucoOrario(
    val orarioInizio: String,
    val orarioFine: String,
    val durataMinuti: Int
)

data class SovrapposizioneOrari(
    val turno1: Turno,
    val turno2: Turno,
    val orarioInizio: String,
    val orarioFine: String
)


/**
 * Calcola lo stato di un dipartimento basandosi sui turni assegnati
 */
private fun calcolaStatoDipartimento(
    dipartimento: AreaLavoro,
    turniDipartimento: List<Turno>,
    orari: Pair<LocalTime, LocalTime>
): RisultatoAnalisiDipartimento {

    // Calcola ore totali richieste
    val oreTotali = calcolaOreTotali(orari.first, orari.second).toDouble()

    // Calcola ore coperte dai turni
    val oreCoperte = turniDipartimento.sumOf { it.calcolaDurata().toDouble() }

    // Calcola percentuale copertura
    val percentualeCopertura = if (oreTotali > 0) {
        (oreCoperte / oreTotali).toFloat()
    } else {
        0f
    }

    // Rileva buchi orari
    val buchi = rilevaBuchiOrari(orari, turniDipartimento)

    // Rileva sovrapposizioni
    val sovrapposizioni = rilevaSovrapposizioni(turniDipartimento)

    // Determina lo stato finale
    val stato = when {
        turniDipartimento.isEmpty() -> DipartimentoStatus.INCOMPLETE
        sovrapposizioni.isNotEmpty() -> DipartimentoStatus.PARTIAL
        buchi.isNotEmpty() -> DipartimentoStatus.PARTIAL
        percentualeCopertura >= 0.95f -> DipartimentoStatus.COMPLETE
        percentualeCopertura >= 0.5f -> DipartimentoStatus.PARTIAL
        else -> DipartimentoStatus.INCOMPLETE
    }

    return RisultatoAnalisiDipartimento(
        stato = stato,
        turniAssegnati = turniDipartimento.size,
        oreCoperte = oreCoperte,
        oreTotali = oreTotali,
        percentualeCopertura = percentualeCopertura,
        buchi = buchi,
        sovrapposizioni = sovrapposizioni
    )
}


private fun rilevaBuchiOrari(
    orari: Pair<LocalTime, LocalTime>,
    turni: List<Turno>
): List<BucoOrario> {
    // 1. Se non ci sono turni, tutto l’orario è un buco
    if (turni.isEmpty()) {
        val durataMinuti = calcolaMinutiTotali(orari.first, orari.second)
        return listOf(
            BucoOrario(
                orarioInizio = orari.first.toString(),
                orarioFine = orari.second.toString(),
                durataMinuti = durataMinuti
            )
        )
    }

    // 2. Ordina e trasforma in lista di intervalli [start, end]
    val intervalli = turni
        .map { it.orarioInizio to it.orarioFine }
        .sortedBy { it.first }

    // 3. Fai il merge degli intervalli sovrapposti o contigui
    val merged = mutableListOf<Pair<LocalTime, LocalTime>>()
    for ((start, end) in intervalli) {
        if (merged.isEmpty()) {
            merged.add(start to end)
        } else {
            val (lastStart, lastEnd) = merged.last()
            if (!start.isAfter(lastEnd)) {
                // si sovrappone o tocca: estendi l’ultimo blocco
                merged[merged.lastIndex] = lastStart to maxOf(lastEnd, end)
            } else {
                // separato: nuovo blocco
                merged.add(start to end)
            }
        }
    }

    // 4. Ora calcola i buchi fra orari.first → merged[0].first,
    //    poi tra merged[i].second → merged[i+1].first,
    //    infine merged.last().second → orari.second
    val buchi = mutableListOf<BucoOrario>()
    val inizioGiornata = orari.first
    val fineGiornata  = orari.second

    // Buco prima del primo blocco
    val (mFirstStart, _) = merged.first()
    if (inizioGiornata.isBefore(mFirstStart)) {
        buchi.add(
            BucoOrario(inizioGiornata.toString(), mFirstStart.toString(),
                Duration.between(inizioGiornata, mFirstStart).toMinutes().toInt())
        )
    }
    // Buchi fra blocchi
    for (i in 0 until merged.size - 1) {
        val (_, endI) = merged[i]
        val (startNext, _) = merged[i + 1]
        if (endI.isBefore(startNext)) {
            buchi.add(
                BucoOrario(endI.toString(), startNext.toString(),
                    Duration.between(endI, startNext).toMinutes().toInt())
            )
        }
    }
    // Buco dopo l’ultimo blocco
    val (_, mLastEnd) = merged.last()
    if (mLastEnd.isBefore(fineGiornata)) {
        buchi.add(
            BucoOrario(mLastEnd.toString(), fineGiornata.toString(),
                Duration.between(mLastEnd, fineGiornata).toMinutes().toInt())
        )
    }

    return buchi
}


/**
 * Rileva le sovrapposizioni tra turni
 */
private fun rilevaSovrapposizioni(turni: List<Turno>): List<SovrapposizioneOrari> {
    val sovrapposizioni = mutableListOf<SovrapposizioneOrari>()

    for (i in turni.indices) {
        for (j in i + 1 until turni.size) {
            val turno1 = turni[i]
            val turno2 = turni[j]

            if (turno1.siSovrappongeCon(turno2)) {
                try {
                    val inizio1 = turno1.orarioInizio
                    val fine1 = turno1.orarioFine
                    val inizio2 = turno2.orarioInizio
                    val fine2 = turno2.orarioFine

                    // Calcola l'intervallo di sovrapposizione
                    val inizioSovrapposizione = if (inizio1.isAfter(inizio2)) inizio1 else inizio2
                    val fineSovrapposizione = if (fine1.isBefore(fine2)) fine1 else fine2

                    sovrapposizioni.add(
                        SovrapposizioneOrari(
                            turno1 = turno1,
                            turno2 = turno2,
                            orarioInizio = inizioSovrapposizione.toString(),
                            orarioFine = fineSovrapposizione.toString()
                        )
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    return sovrapposizioni
}

private fun calcolaOreTotali(inizio: LocalTime, fine: LocalTime): Int {
    return Duration.between(inizio, fine).toHours().toInt()
}

private fun calcolaMinutiTotali(inizio: LocalTime, fine: LocalTime): Int {
    return Duration.between(inizio, fine).toMinutes().toInt()
}