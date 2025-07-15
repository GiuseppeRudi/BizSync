package com.bizsync.app.screens
// Import necessari da aggiungere
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.OrarioGiornoRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun IniziaPubblicazioneScreen(
    publishableWeek: LocalDate?,
    onStartPlanning: () -> Unit,
    onGoToGestioneAziendale: () -> Unit = {} // Callback per navigare alla gestione aziendale
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()

    val dipartimenti = userState.azienda.areeLavoro

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pianifica Turni",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pianifica Turni",
                    style = MaterialTheme.typography.headlineMedium
                )

                publishableWeek?.let { week ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Settimana: ${week.format(DateTimeFormatter.ofPattern("dd/MM"))} - ${week.plusDays(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Spiegazione orari
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Orari Target Settimana",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "I turni della settimana saranno pianificati utilizzando gli orari di apertura e chiusura configurati per ogni dipartimento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Lista dipartimenti con orari
        if (dipartimenti.isNotEmpty()) {
            item {
                Text(
                    text = "Dipartimenti e Orari",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(dipartimenti) { dipartimento ->
                DipartimentoCard(dipartimento = dipartimento)
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nessun dipartimento configurato",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Configura almeno un dipartimento prima di iniziare la pianificazione.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Pulsanti azione
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pulsante per modificare orari
                OutlinedButton(
                    onClick = onGoToGestioneAziendale,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modifica Orari Dipartimenti")
                }

                // Pulsante principale
                Button(
                    onClick = onStartPlanning,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = dipartimenti.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inizia Pubblicazione")
                }

                if (dipartimenti.isEmpty()) {
                    Text(
                        text = "Configura almeno un dipartimento per procedere",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DipartimentoCard(dipartimento: AreaLavoro) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header dipartimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dipartimento.nomeArea,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orari settimanali
            if (dipartimento.orariSettimanali.isNotEmpty()) {
                dipartimento.orariSettimanali.forEach { (giorno, orari) ->
                    OrarioGiornoRow(
                        giorno = giorno,
                        orarioInizio = orari.first.toString(),
                        orarioFine = orari.second.toString()
                    )
                }
            } else {
                Text(
                    text = "Nessun orario configurato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

