package com.bizsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.domain.model.Nota
import com.bizsync.domain.model.Pausa
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.constants.enumClass.TipoPausa

@Composable
fun AIResultDialog(
    dipendenti: List<User>,
    turniGenerati: List<Turno>,
    message: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = {
            Text("Turni Generati con AI")
        },
        text = {
            Column {
                message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }


                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(turniGenerati) { turno ->
                        TurnoDetailCard(
                            turno = turno,
                            dipendenti = dipendenti
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = turniGenerati.isNotEmpty()
            ) {
                Text("Conferma e Aggiungi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
private fun TurnoDetailCard(
    turno: Turno,
    dipendenti: List<User>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header del turno
            Text(
                text = turno.titolo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Orari
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${turno.orarioInizio} - ${turno.orarioFine} (${turno.calcolaDurata()}h)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Dipendenti
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val nomiDipendenti = getNomiDipendenti(turno.idDipendenti, dipendenti)

                    if (nomiDipendenti.isNotEmpty()) {
                        Text(
                            text = formatNomiDipendenti(nomiDipendenti),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "Nessun dipendente assegnato",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Note generate dall'AI
            if (turno.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Note (${turno.note.size}):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        turno.note.take(2).forEach { nota ->
                            NoteChip(nota = nota)
                        }

                        if (turno.note.size > 2) {
                            Text(
                                text = "+${turno.note.size - 2} altre note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Pause generate dall'AI
            if (turno.pause.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            text = "Pause (${turno.pause.size}):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        turno.pause.forEach { pausa ->
                            PausaChip(pausa = pausa)
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun NoteChip(nota: Nota) {
    val backgroundColor = when (nota.tipo) {
        TipoNota.IMPORTANTE -> MaterialTheme.colorScheme.errorContainer
        TipoNota.SICUREZZA -> MaterialTheme.colorScheme.errorContainer
        TipoNota.CLIENTE -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val textColor = when (nota.tipo) {
        TipoNota.IMPORTANTE -> MaterialTheme.colorScheme.onErrorContainer
        TipoNota.SICUREZZA -> MaterialTheme.colorScheme.onErrorContainer
        TipoNota.CLIENTE -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "${nota.tipo.name}: ${nota.getTestoAbbreviato(60)}",
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PausaChip(pausa: Pausa) {
    val durataMinuti = pausa.durata.toMinutes()
    val backgroundColor = if (pausa.èRetribuita) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val textColor = if (pausa.èRetribuita) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${pausa.tipo.name.replace("_", " ")}: ${durataMinuti}min",
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )

            if (pausa.èRetribuita) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Paid,
                    contentDescription = "Retribuita",
                    modifier = Modifier.size(12.dp),
                    tint = textColor
                )
            }
        }
    }
}

/**
 * Recupera i nomi e cognomi dei dipendenti dai loro ID
 */
private fun getNomiDipendenti(idDipendenti: List<String>, dipendenti: List<User>): List<String> {
    return idDipendenti.mapNotNull { id ->
        dipendenti.find { it.uid == id }?.let { user ->
            "${user.nome} ${user.cognome}".trim()
        }
    }
}

/**
 * Formatta i nomi dei dipendenti per la visualizzazione
 */
private fun formatNomiDipendenti(nomi: List<String>): String {
    return when {
        nomi.isEmpty() -> "Nessun dipendente"
        nomi.size <= 3 -> nomi.joinToString(", ")
        else -> "${nomi.take(2).joinToString(", ")} e altri ${nomi.size - 2}"
    }
}