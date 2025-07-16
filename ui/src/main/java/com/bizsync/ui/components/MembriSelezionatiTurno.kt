package com.bizsync.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.DipendentiGiorno
import com.bizsync.domain.model.User
import java.time.LocalTime


// Aggiornamento del componente MembriSelezionatiSummary
@Composable
fun MembriSelezionatiSummary(
    dipendenti: List<User>,
    membriSelezionati: List<User>,
    orarioInizio: LocalTime?,
    orarioFine: LocalTime?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orariImpostati = orarioInizio != null && orarioFine != null

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = (if (orariImpostati) onClick else { }) as () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dipendenti assegnati",
                    style = MaterialTheme.typography.titleMedium
                )

                when {
                    !orariImpostati -> {
                        Text(
                            text = "Imposta prima l'orario del turno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    membriSelezionati.isEmpty() -> {
                        Text(
                            text = "Nessun dipendente selezionato",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        Text(
                            text = "${membriSelezionati.size} dipendenti selezionati",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Mostra i primi 3 nomi
                        val nomiDaMostrare = membriSelezionati.take(3)
                        val nomiStringa = nomiDaMostrare.joinToString(", ") {
                            if (it.nome.isNotBlank() && it.cognome.isNotBlank()) {
                                "${it.nome} ${it.cognome}"
                            } else {
                                it.email
                            }
                        }
                        val altriCount = membriSelezionati.size - nomiDaMostrare.size

                        Text(
                            text = if (altriCount > 0) "$nomiStringa e altri $altriCount" else nomiStringa,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (orariImpostati) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Seleziona dipendenti"
                )
            } else {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Imposta orario prima",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembriSelectionDialog(
    showDialog: Boolean,
    disponibiliMembri: DipendentiGiorno,
    membriSelezionati: List<String>,
    orarioInizio: LocalTime,
    orarioFine: LocalTime,
    onDismiss: () -> Unit,
    onMembriUpdated: (List<String>) -> Unit
) {
    if (showDialog) {
        var tempSelection by remember { mutableStateOf(membriSelezionati) }

        // Filtra i dipendenti in base alla disponibilità
        val (disponibili, nonDisponibili) = dividiDipendentiPerDisponibilita(
            disponibiliMembri = disponibiliMembri,
            orarioInizio = orarioInizio,
            orarioFine = orarioFine
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("Seleziona Dipendenti")
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sezione dipendenti disponibili
                    if (disponibili.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dipendenti disponibili (${disponibili.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(disponibili) { membro ->
                            DipendenteSelectableItem(
                                membro = membro,
                                isSelected = tempSelection.contains(membro.uid),
                                isDisponibile = true,
                                motivoIndisponibilita = null,
                                onSelectionChanged = { isSelected ->
                                    tempSelection = if (isSelected) {
                                        tempSelection + membro.uid
                                    } else {
                                        tempSelection - membro.uid
                                    }
                                }
                            )
                        }
                    }

                    // Sezione dipendenti non disponibili
                    if (nonDisponibili.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dipendenti non disponibili (${nonDisponibili.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        items(nonDisponibili) { (membro, motivo) ->
                            DipendenteSelectableItem(
                                membro = membro,
                                isSelected = tempSelection.contains(membro.uid),
                                isDisponibile = false,
                                motivoIndisponibilita = motivo,
                                onSelectionChanged = { }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMembriUpdated(tempSelection)
                        onDismiss()
                    }
                ) {
                    val disponibiliSelezionati = tempSelection.count { id ->
                        disponibili.any { it.uid == id }
                    }
                    val nonDisponibiliSelezionati = tempSelection.count { id ->
                        nonDisponibili.any { it.first.uid == id }
                    }

                    Text(
                        if (nonDisponibiliSelezionati > 0) {
                            "Conferma (${disponibiliSelezionati}+${nonDisponibiliSelezionati}⚠️)"
                        } else {
                            "Conferma (${disponibiliSelezionati})"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun DipendenteSelectableItem(
    membro: User,
    isSelected: Boolean,
    isDisponibile: Boolean,
    motivoIndisponibilita: String?,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChanged(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected && isDisponibile -> MaterialTheme.colorScheme.primaryContainer
                isSelected && !isDisponibile -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                !isDisponibile -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (membro.nome.isNotBlank() && membro.cognome.isNotBlank()) {
                        "${membro.nome} ${membro.cognome}"
                    } else {
                        membro.email
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDisponibile)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (membro.nome.isNotBlank() && membro.cognome.isNotBlank()) {
                    Text(
                        text = membro.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Mostra motivo indisponibilità
                if (!isDisponibile && motivoIndisponibilita != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = motivoIndisponibilita,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Icona di selezione
            when {isSelected && isDisponibile -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selezionato",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            }
        }
    }
}

// Funzione helper per dividere i dipendenti per disponibilità
private fun dividiDipendentiPerDisponibilita(
    disponibiliMembri: DipendentiGiorno,
    orarioInizio: LocalTime,
    orarioFine: LocalTime
): Pair<List<User>, List<Pair<User, String>>> {
    val disponibili = mutableListOf<User>()
    val nonDisponibili = mutableListOf<Pair<User, String>>()

    disponibiliMembri.utenti.forEach { utente ->
        val stato = disponibiliMembri.statoPerUtente[utente.uid]

        if (stato == null) {
            // Nessun stato = disponibile
            disponibili.add(utente)
        } else {
            val motivo = when {
                stato.isAssenteTotale -> "Assente per tutta la giornata"
                stato.turnoAssegnato -> "Già assegnato ad un altro turno"
                stato.assenzaParziale != null -> {
                    val assenza = stato.assenzaParziale!!
                    if (orariSiSovrappongono(orarioInizio, orarioFine, assenza.inizio, assenza.fine)) {
                        "Assente dalle ${assenza.inizio} alle ${assenza.fine}"
                    } else {
                        null // Non si sovrappone, è disponibile
                    }
                }
                else -> null
            }

            if (motivo != null) {
                nonDisponibili.add(utente to motivo)
            } else {
                disponibili.add(utente)
            }
        }
    }

    return disponibili to nonDisponibili
}

// Funzione helper per verificare sovrapposizione orari
private fun orariSiSovrappongono(
    turnoInizio: LocalTime,
    turnoFine: LocalTime,
    assenzaInizio: LocalTime,
    assenzaFine: LocalTime
): Boolean {
    return turnoInizio.isBefore(assenzaFine) && turnoFine.isAfter(assenzaInizio)
}

