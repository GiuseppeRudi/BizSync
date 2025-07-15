package com.bizsync.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Turno
import com.bizsync.ui.viewmodels.DipartimentoStatus
import java.time.LocalDate
import java.time.LocalTime


data class ConfirmDialogData(
    val dipartimento: AreaLavoro,
    val giorno: LocalDate,
    val stato: DipartimentoStatus
)

// Dialog di conferma completamento
@Composable
fun ConfirmCompletionDialog(
    dialogData: ConfirmDialogData,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val orari = dialogData.dipartimento.orariSettimanali[dialogData.giorno.dayOfWeek]
    val oreTotali = orari?.let { calcolaOreTotali(it.first, it.second) } ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (dialogData.stato) {
                        DipartimentoStatus.PARTIAL -> Icons.Default.Warning
                        DipartimentoStatus.INCOMPLETE -> Icons.Default.Error
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when (dialogData.stato) {
                        DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                        DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conferma Completamento")
            }
        },
        text = {
            Column {
                Text(
                    text = "Dipartimento: ${dialogData.dipartimento.nomeArea}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (dialogData.stato) {
                    DipartimentoStatus.INCOMPLETE -> {
                        Text(
                            text = "⚠️ Non hai ancora assegnato alcun turno per questo dipartimento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Orario richiesto: ${orari?.first} - ${orari?.second} ($oreTotali ore)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DipartimentoStatus.PARTIAL -> {
                        Text(
                            text = "⚠️ Hai ancora ore scoperte in questo dipartimento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Potrebbero esserci dei buchi nella copertura oraria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Text(
                            text = "✅ Tutte le ore sono coperte correttamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vuoi comunque segnare il dipartimento come completato?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (dialogData.stato) {
                        DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.error
                        DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text("Segna come Completato")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Torna a Modificare")
            }
        }
    )
}

// Funzioni utility private
private fun calcolaOreTotali(inizio: LocalTime, fine: LocalTime): Int {
    return inizio.until(fine, java.time.temporal.ChronoUnit.HOURS).toInt()
}

//private fun calcolaOreAssegnate(turni: List<Turno>): Int {
//    // TODO: implementare calcolo basato su orari effettivi dei turni
//    return turni.sumOf { turno ->
//        // Assumendo che ogni turno abbia orario inizio/fine
//        // turno.durata o calcolo da turno.orarioInizio/orarioFine
//        8 // Placeholder: 8 ore per turno
//    }
//}
