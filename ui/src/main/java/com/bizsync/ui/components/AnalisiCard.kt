//package com.bizsync.ui.components
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material.icons.filled.Warning
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bizsync.domain.model.AreaLavoro
//import com.bizsync.domain.model.Turno
//import com.bizsync.ui.viewmodels.DipartimentoStatus
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//
//
//@Composable
// fun AnalisiCoperturaCard(
//    dipartimento: AreaLavoro,
//    giornoSelezionato: LocalDate,
//    turniAssegnati: List<Turno>
//) {
//    val dayOfWeek = giornoSelezionato.dayOfWeek
//    val orari = dipartimento.orariSettimanali[dayOfWeek] ?: return
//
//    val oreTotali = calcolaOreTotali(orari.first, orari.second)
//    val oreAssegnate = calcolaOreAssegnate(turniAssegnati)
//    val buchi = calcolaBuchiOrari(orari, turniAssegnati)
//    val sovrapposizioni = calcolaSovrapposizioni(turniAssegnati)
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.secondaryContainer
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Analisi Copertura",
//                style = MaterialTheme.typography.titleMedium,
//                color = MaterialTheme.colorScheme.onSecondaryContainer
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Statistiche
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                StatisticaCard("Ore Totali", "$oreTotali h")
//                StatisticaCard("Ore Assegnate", "$oreAssegnate h")
//                StatisticaCard("Copertura", "${(oreAssegnate * 100 / oreTotali.coerceAtLeast(1))}%")
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Problemi rilevati
//            if (buchi.isNotEmpty() || sovrapposizioni.isNotEmpty()) {
//                Text(
//                    text = "Problemi Rilevati:",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Medium,
//                    color = MaterialTheme.colorScheme.error
//                )
//
//                buchi.forEach { buco ->
//                    Text(
//                        text = "• Buco: ${buco.first} - ${buco.second}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//
//                sovrapposizioni.forEach { sovrapposizione ->
//                    Text(
//                        text = "• Sovrapposizione: ${sovrapposizione.first} - ${sovrapposizione.second}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            } else {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Default.CheckCircle,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Nessun problema rilevato",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
// fun StatisticaCard(label: String, value: String) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = value,
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.onSecondaryContainer
//        )
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSecondaryContainer
//        )
//    }
//}
//
//@Composable
// fun AzioniCompletamentoCard(
//    stato: DipartimentoStatus,
//    onSegnaCompletato: () -> Unit,
//    onTornaIndietro: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            OutlinedButton(
//                onClick = onTornaIndietro,
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(Icons.Default.ArrowBack, contentDescription = null)
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Indietro")
//            }
//
//            Button(
//                onClick = onSegnaCompletato,
//                modifier = Modifier.weight(1f),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = when (stato) {
//                        DipartimentoStatus.COMPLETE -> MaterialTheme.colorScheme.primary
//                        DipartimentoStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
//                        DipartimentoStatus.INCOMPLETE -> MaterialTheme.colorScheme.error
//                    }
//                )
//            ) {
//                Icon(
//                    when (stato) {
//                        DipartimentoStatus.COMPLETE -> Icons.Default.CheckCircle
//                        DipartimentoStatus.PARTIAL -> Icons.Default.Warning
//                        DipartimentoStatus.INCOMPLETE -> Icons.Default.Error
//                    },
//                    contentDescription = null
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Completato")
//            }
//        }
//    }
//}
//
//private fun calcolaBuchiOrari(
//    orari: Pair<String, String>,
//    turni: List<Turno>
//): List<Pair<String, String>> {
//    // TODO: implementare calcolo buchi orari
//    return emptyList()
//}
//
//private fun calcolaSovrapposizioni(turni: List<Turno>): List<Pair<String, String>> {
//    // TODO: implementare calcolo sovrapposizioni
//    return emptyList()
//}
//
//// Componenti UI mancanti per GestioneTurniDipartimentoScreen
//
//
//
//
//// Funzioni utility necessarie
//
//private fun calcolaOreTotali(inizio: String, fine: String): Int {
//    return try {
//        val inizioTime = LocalTime.parse(inizio, DateTimeFormatter.ofPattern("HH:mm"))
//        val fineTime = LocalTime.parse(fine, DateTimeFormatter.ofPattern("HH:mm"))
//
//        val durataMinuti = fineTime.toSecondOfDay() - inizioTime.toSecondOfDay()
//        (durataMinuti / 3600).toInt()
//    } catch (e: Exception) {
//        8 // Default 8 ore
//    }
//}
//
//private fun calcolaOreAssegnate(turni: List<Turno>): Int {
//    return turni.sumOf { turno ->
//        try {
//            val inizio = turno.orarioInizio
//            val fine = turno.orarioFine
//
//            val durataMinuti = fine.toSecondOfDay() - inizio.toSecondOfDay()
//            (durataMinuti / 3600).toInt()
//        } catch (e: Exception) {
//            0
//        }
//    }
//}
