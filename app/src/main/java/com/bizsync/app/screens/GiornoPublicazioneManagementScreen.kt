package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.ui.viewmodels.CompanyViewModel
import com.bizsync.ui.viewmodels.UserViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiornoPublicazioneManagementScreen(
    companyVm: CompanyViewModel,
    userVm : UserViewModel,
    idAzienda: String
) {
    val uiState by companyVm.uiState.collectAsState()
    val scaffoldVM = LocalScaffoldViewModel.current

    val userState by userVm.uiState.collectAsState()

    val giornoAttualePublicazione = userState.azienda.giornoPubblicazioneTurni

    val showDialog = uiState.showGiornoPublicazioneDialog
    val giornoTemp = uiState.giornoPublicazioneTemp
    val hasChanges = uiState.hasGiornoPublicazioneChanges
    val isLoading = uiState.isLoading

    LaunchedEffect(uiState.hasGiornoPubblicato) {
        if(uiState.hasGiornoPubblicato && giornoTemp!=null){
            userVm.setGiornoPublicazioneTurni(giornoTemp)
            companyVm.setHasGiornoPubblicazioneChanges(false)
            companyVm.setGiornoPublicazioneTempNull()
        }
    }


    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { companyVm.setSelectedOperation(null) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }

                    Column {
                        Text(
                            text = "Giorno Pubblicazione",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestisci quando pubblichi i turni",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Contenuto principale
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card informativa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Informazioni importanti",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "⚠️ Il cambiamento del giorno di pubblicazione non influenzerà la settimana successiva, ma quella ancora dopo, per dare tempo ai dipendenti di adattarsi al nuovo programma.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Card giorno attuale
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Giorno attuale di pubblicazione",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = giornoAttualePublicazione.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { companyVm.openGiornoPublicazioneDialog(giornoAttualePublicazione) },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifica Giorno")
                    }
                }
            }

            // Card suggerimenti
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Suggerimenti",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Venerdì è il giorno più comune per pubblicare i turni della settimana successiva\n" +
                                "• Evita di pubblicare nel weekend per garantire che tutti i dipendenti vedano la notifica\n" +
                                "• Considera i giorni di riposo del tuo team management",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    // Dialog per selezione giorno
    if (showDialog && giornoTemp != null) {
        GiornoPublicazioneDialog(
            giornoAttuale = giornoAttualePublicazione,
            giornoSelezionato = giornoTemp,
            onDismiss = { companyVm.closeGiornoPublicazioneDialog() },
            onGiornoSelected = { giorno -> companyVm.setGiornoPublicazioneTemp(giorno) },
            onSalva = { companyVm.salvaGiornoPublicazione(idAzienda) },
            hasChanges = hasChanges,
            isLoading = isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiornoPublicazioneDialog(
    giornoAttuale: DayOfWeek,
    giornoSelezionato: DayOfWeek,
    onDismiss: () -> Unit,
    onGiornoSelected: (DayOfWeek) -> Unit,
    onSalva: () -> Unit,
    hasChanges: Boolean,
    isLoading: Boolean
) {
    val giorni = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleziona Giorno di Pubblicazione",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                // Avvertimento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Attenzione",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "Il cambiamento sarà effettivo dalla settimana dopo la prossima",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista giorni
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(giorni) { giorno ->
                        GiornoSelezionabileCard(
                            giorno = giorno,
                            isSelected = giornoSelezionato == giorno,
                            isAttuale = giornoAttuale == giorno,
                            onClick = { onGiornoSelected(giorno) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSalva,
                enabled = hasChanges && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salva")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun GiornoSelezionabileCard(
    giorno: DayOfWeek,
    isSelected: Boolean,
    isAttuale: Boolean,
    onClick: () -> Unit
) {
    val nomeGiorno = giorno.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
        .replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isAttuale -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isSelected -> CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
            else -> null
        }
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
                    text = nomeGiorno,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )

                if (isAttuale) {
                    Text(
                        text = "Attuale",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selezionato",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
