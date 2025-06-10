package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.AiBanner
import com.bizsync.ui.components.UniversalCard
import com.bizsync.ui.viewmodels.OnBoardingPianificaViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPianificaScreen(
    onSetupComplete: () -> Unit,
) {

    val viewModel : OnBoardingPianificaViewModel = hiltViewModel()
    val currentStep by viewModel.currentStep.collectAsState()

    val scaffoldVM = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (currentStep + 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        when (currentStep) {
            0 -> { WelcomeStep(viewModel) }
            1 -> { AreeLavoroStep(viewModel) }
            2 -> { TurniFrequentiStep(viewModel) }
        }
    }
}

@Composable
fun WelcomeStep(viewModel: OnBoardingPianificaViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Benvenuto in BizSync! 🎯",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Per iniziare a pianificare i turni, dobbiamo configurare alcuni parametri essenziali:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("📍 Aree di Lavoro", fontWeight = FontWeight.Bold)
                Text("Definisci le zone operative della tua attività")

                Spacer(modifier = Modifier.height(12.dp))

                Text("⏰ Turni Frequenti", fontWeight = FontWeight.Bold)
                Text("Configura i turni più utilizzati per velocizzare la pianificazione")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {viewModel.setStep(1)},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inizia Configurazione")
        }
    }
}

@Composable
fun AreeLavoroStep(viewModel: OnBoardingPianificaViewModel) {
    val areeDefaultAttive by viewModel.aree.collectAsState()
    val nuovaArea by viewModel.nuovaArea.collectAsState()
    val checkAreeDefualt by viewModel.areePronte.collectAsState()

    val totalAree = areeDefaultAttive.size
    val maxAree = 10
    val canAddArea = totalAree < maxAree

    val userViewModel = LocalUserViewModel.current
    val azienda by userViewModel.azienda.collectAsState()

    LaunchedEffect(azienda) {
        if (!checkAreeDefualt) {
            viewModel.generaAreeAi(azienda.Nome)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- UI principale ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Configura le Aree di Lavoro",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Rimuovi aree predefinite non necessarie o aggiungi le tue personalizzate (massimo $maxAree aree)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Aree attive: $totalAree/$maxAree",
                style = MaterialTheme.typography.bodySmall,
                color = if (totalAree >= maxAree) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Aree Predefinite:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    AiBanner(azienda.Nome, checkAreeDefualt)
                }

                if (!checkAreeDefualt) {
                    items(10) {
                        UniversalCard(loading = true)
                    }
                } else {
                    items(areeDefaultAttive) { area ->
                        UniversalCard(
                            loading = false,
                            title = area.nomeArea,
                            showDelete = true,
                            onDelete = { viewModel.onRimuoviAreaById(area.id) }
                        )
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aggiungi Area Personalizzata:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuovaArea.nomeArea,
                            onValueChange = { viewModel.onNuovaAreaChangeName(it) },
                            label = { Text("Nome area") },
                            modifier = Modifier.weight(1f),
                            enabled = canAddArea,
                            supportingText = {
                                if (totalAree >= maxAree) {
                                    Text(
                                        "Limite massimo raggiunto. Rimuovi un'area per aggiungerne una nuova.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )

                        IconButton(
                            onClick = {
                                viewModel.aggiungiArea()
                                viewModel.resetNuovaArea()
                            },
                            enabled = canAddArea
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aggiungi",
                                tint = if (canAddArea) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { viewModel.setStep(0) }) {
                    Text("Indietro")
                }

                Button(
                    onClick = { viewModel.setStep(2) },
                    enabled = totalAree > 0
                ) {
                    Text("Continua")
                }
            }
        }

    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniFrequentiStep(viewModel: OnBoardingPianificaViewModel) {
    val turni by viewModel.turni.collectAsState()
    val nuovoTurno by viewModel.nuovoTurno.collectAsState()
    val checkTurniPronti by viewModel.turniPronti.collectAsState()

    val totalTurni = turni.size
    val maxTurni = 6
    val canAddTurno = totalTurni < maxTurni

    val userViewModel = LocalUserViewModel.current
    val azienda by userViewModel.azienda.collectAsState()

    LaunchedEffect(azienda) {
        if (!checkTurniPronti) {  // Solo se i turni non sono ancora pronti
            viewModel.generaTurniAi(azienda.Nome)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Configura i Turni Frequenti",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "I turni qui configurati saranno suggeriti durante la creazione (massimo $maxTurni turni)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Turni attivi: $totalTurni/$maxTurni",
                style = MaterialTheme.typography.bodySmall,
                color = if (totalTurni >= maxTurni) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                item {
                    AiBanner(azienda.Nome, checkTurniPronti)
                }

                if (!checkTurniPronti) {
                    items(6) {
                        UniversalCard(loading = true)
                    }
                } else {
                    items(turni) { turno ->
                        UniversalCard(
                            loading = false,
                            title = turno.nome,
                            subtitle = "${turno.oraInizio} - ${turno.oraFine}",
                            showDelete = true,
                            onDelete = { viewModel.onRimuoviTurnoById(turno.id) }
                        )
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aggiungi Nuovo Turno:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = nuovoTurno.nome,
                                onValueChange = { viewModel.onNewTurnoChangeName(it) },
                                label = { Text("Nome turno") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canAddTurno,
                                supportingText = {
                                    if (totalTurni >= maxTurni) {
                                        Text(
                                            "Limite massimo raggiunto. Rimuovi un turno per aggiungerne uno nuovo.",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = nuovoTurno.oraInizio,
                                    onValueChange = { viewModel.onNewTurnoChangeStartDate(it) },
                                    label = { Text("Ora inizio (HH:MM)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    enabled = canAddTurno
                                )

                                OutlinedTextField(
                                    value = nuovoTurno.oraFine,
                                    onValueChange = { viewModel.onNewTurnoChangeFinishDate(it) },
                                    label = { Text("Ora fine (HH:MM)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    enabled = canAddTurno
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.aggiungiTurno() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canAddTurno &&
                                        nuovoTurno.nome.isNotBlank() &&
                                        nuovoTurno.oraInizio.isNotBlank() &&
                                        nuovoTurno.oraFine.isNotBlank()
                            ) {
                                Text("Aggiungi Turno")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { viewModel.setStep(1) }) {
                    Text("Indietro")
                }

                Button(
                    onClick = { /* Azione completamento */ },
                    enabled = totalTurni > 0
                ) {
                    Text("Completa Setup")
                }
            }
        }
    }
}