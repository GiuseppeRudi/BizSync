package com.bizsync.app.screens

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
import com.bizsync.domain.model.WeeklyShift
import com.bizsync.ui.components.EmptyDayCard
import com.bizsync.ui.components.GiornoHeaderCard
import com.bizsync.ui.components.RiepilogoGiornataCard
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
    weeklyShift: WeeklyShift?,
) {

    val managerState by managerVM.uiState.collectAsState()
    val dayOfWeek = giornoSelezionato.dayOfWeek

    val turniGiorno = managerState.turniGiornalieri

    val dipartimentiDelGiorno = dipartimenti
        .filter { it.orariSettimanali.containsKey(dayOfWeek) }
        .sortedBy { it.orariSettimanali[dayOfWeek]?.first ?: LocalTime.MIN }


    LaunchedEffect(giornoSelezionato) {
        managerVM.setLoading(true)
        if (weeklyShift != null) {
            managerVM.setTurniGiornalieri(dayOfWeek, dipartimentiDelGiorno)
        }
    }


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

        item {
            RiepilogoGiornataCard(dipartimentiDelGiorno)
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
                            text = "${risultatiAnalisi.buchi.size} buchi orari",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (risultatiAnalisi.sovrapposizioni.isNotEmpty()) {
                        Text(
                            text = "${risultatiAnalisi.sovrapposizioni.size} sovrapposizioni",
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
        sovrapposizioni.isNotEmpty() -> DipartimentoStatus.PARTIAL // Problemi da risolvere
        buchi.isNotEmpty() -> DipartimentoStatus.PARTIAL // Copertura incompleta
        percentualeCopertura >= 0.95f -> DipartimentoStatus.COMPLETE // Copertura quasi totale
        percentualeCopertura >= 0.5f -> DipartimentoStatus.PARTIAL // Copertura parziale
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

/**
 * Rileva i buchi nella copertura oraria
 */
private fun rilevaBuchiOrari(
    orari: Pair<LocalTime, LocalTime>,
    turni: List<Turno>
): List<BucoOrario> {
    if (turni.isEmpty()) {
        // Se non ci sono turni, tutto l'orario è un buco
        val durataMinuti = calcolaMinutiTotali(orari.first, orari.second)
        return listOf(
            BucoOrario(
                orarioInizio = orari.first.toString(),
                orarioFine = orari.second.toString(),
                durataMinuti = durataMinuti
            )
        )
    }

    val buchi = mutableListOf<BucoOrario>()

    try {
        val inizioGiornata = orari.first
        val fineGiornata = orari.second

        // Ordina i turni per orario di inizio
        val turniOrdinati = turni.sortedBy { it.orarioInizio }

        // Controlla buco prima del primo turno
        val primoTurno = turniOrdinati.first()
        val inizioPrimoTurno = primoTurno.orarioInizio

        if (inizioGiornata.isBefore(inizioPrimoTurno)) {
            val durataMinuti = Duration.between(inizioGiornata, inizioPrimoTurno).toMinutes().toInt()
            buchi.add(
                BucoOrario(
                    orarioInizio = inizioGiornata.toString(),
                    orarioFine = inizioPrimoTurno.toString(),
                    durataMinuti = durataMinuti
                )
            )
        }

        // Controlla buchi tra i turni
        for (i in 0 until turniOrdinati.size - 1) {
            val turnoCorrente = turniOrdinati[i]
            val turnoSuccessivo = turniOrdinati[i + 1]

            val fineCorrente = turnoCorrente.orarioFine
            val inizioSuccessivo = turnoSuccessivo.orarioInizio

            if (fineCorrente.isBefore(inizioSuccessivo)) {
                val durataMinuti = Duration.between(fineCorrente, inizioSuccessivo).toMinutes().toInt()
                buchi.add(
                    BucoOrario(
                        orarioInizio = fineCorrente.toString(),
                        orarioFine = inizioSuccessivo.toString(),
                        durataMinuti = durataMinuti
                    )
                )
            }
        }

        // Controlla buco dopo l'ultimo turno
        val ultimoTurno = turniOrdinati.last()
        val fineUltimoTurno = ultimoTurno.orarioFine

        if (fineUltimoTurno.isBefore(fineGiornata)) {
            val durataMinuti = Duration.between(fineUltimoTurno, fineGiornata).toMinutes().toInt()
            buchi.add(
                BucoOrario(
                    orarioInizio = fineUltimoTurno.toString(),
                    orarioFine = fineGiornata.toString(),
                    durataMinuti = durataMinuti
                )
            )
        }

    } catch (e: Exception) {
        // In caso di errore, non restituire buchi
        return emptyList()
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