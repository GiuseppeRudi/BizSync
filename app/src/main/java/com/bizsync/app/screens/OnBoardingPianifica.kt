package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.AiBanner
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.components.UniversalCard
import com.bizsync.ui.viewmodels.OnBoardingPianificaViewModel
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPianificaScreen(
    onSetupComplete: () -> Unit,
) {

    val viewModel : OnBoardingPianificaViewModel = hiltViewModel()
    val pianificaState by viewModel.uiState.collectAsState()
    val currentStep = pianificaState.currentStep
    val scaffoldVM = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { (currentStep + 1) / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        when (currentStep) {
            0 -> { WelcomeStep(viewModel) }
            1 -> { AreeLavoroStep(viewModel) }
            2 -> { SelezioneAreeOrariStep(viewModel) }
            3 -> { ModificaOrariStep(viewModel) }
            4 -> { TurniFrequentiStep(viewModel,onSetupComplete,scaffoldVM) }
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

                Text("📅 Orari Settimanali", fontWeight = FontWeight.Bold)
                Text("Configura gli orari di lavoro per ogni area")

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
    val pianificaState by viewModel.uiState.collectAsState()
    val aree = pianificaState.aree
    val nuovaArea = pianificaState.nuovaArea
    val checkAreeDefualt = pianificaState.areePronte

    val totalAree = aree.size
    val maxAree = 10
    val canAddArea = totalAree < maxAree

    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()
    val azienda  = userState.azienda

    LaunchedEffect(azienda) {
        if (!checkAreeDefualt) {
            viewModel.generaAreeAi(azienda.nome)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    AiBanner(azienda.nome, checkAreeDefualt)
                }

                if (!checkAreeDefualt) {
                    items(10) {
                        UniversalCard(loading = true)
                    }
                } else {
                    items(aree) { area ->
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

@Composable
fun SelezioneAreeOrariStep(viewModel: OnBoardingPianificaViewModel) {
    val pianificaState by viewModel.uiState.collectAsState()
    val aree = pianificaState.aree
    val selectedAree = pianificaState.selectedAree
    val areeOrariConfigurati = pianificaState.areeOrariConfigurati // Nuova proprietà per tracciare le aree configurate

    // Calcola le aree configurate e non configurate
    val areeConfigurate = aree.filter { areeOrariConfigurati.contains(it.id) }
    val areeNonConfigurate = aree.filter { !areeOrariConfigurati.contains(it.id) }
    val tutteAreeConfigurate = areeNonConfigurate.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seleziona Aree per Orari",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Seleziona le aree che condividono gli stessi orari di lavoro. Potrai configurarli nel prossimo step.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Progresso configurazione
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (tutteAreeConfigurate)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tutteAreeConfigurate) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Completato",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = "${areeConfigurate.size}/${aree.size} aree configurate",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (tutteAreeConfigurate)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (selectedAree.isNotEmpty()) {
            Text(
                text = "${selectedAree.size} area/e selezionata/e per la prossima configurazione",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Card per selezione aree
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Sezione aree già configurate
                if (areeConfigurate.isNotEmpty()) {
                    Text(
                        text = "✅ Aree già configurate:",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    areeConfigurate.forEach { area ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Configurata",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = area.nomeArea,
                                modifier = Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (areeNonConfigurate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Sezione aree da configurare
                if (areeNonConfigurate.isNotEmpty()) {
                    Text(
                        text = "Aree da configurare:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectAllAreeNonConfigurate(areeNonConfigurate.map { it.id })
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Seleziona Tutte")
                        }

                        OutlinedButton(
                            onClick = { viewModel.deselectAllAree() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deseleziona")
                        }
                    }

                    LazyColumn {
                        items(areeNonConfigurate) { area ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedAree.contains(area.id),
                                    onCheckedChange = { isChecked ->
                                        viewModel.onAreaSelectionChanged(area.id, isChecked)
                                    }
                                )
                                Text(
                                    text = area.nomeArea,
                                    modifier = Modifier.padding(start = 12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                } else {
                    // Tutte le aree sono configurate
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completato",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tutte le aree sono state configurate!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Ora puoi procedere alla configurazione dei turni frequenti.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Pulsanti di navigazione
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setStep(1) }) {
                Text("Indietro")
            }

            if (tutteAreeConfigurate) {
                Button(onClick = { viewModel.setStep(4) }) {
                    Text("Continua ai Turni")
                }
            } else {
                Button(
                    onClick = { viewModel.setStep(3) },
                    enabled = selectedAree.isNotEmpty()
                ) {
                    Text("Configura Orari")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificaOrariStep(viewModel: OnBoardingPianificaViewModel) {
    val pianificaState by viewModel.uiState.collectAsState()
    val selectedAree = pianificaState.selectedAree
    val orariTemp = pianificaState.orariTemp
    val aree = pianificaState.aree

    val giorni = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

    val selectedAreaNames = aree.filter { selectedAree.contains(it.id) }.map { it.nomeArea }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configura Orari di Lavoro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Imposta gli orari che verranno applicati a: ${selectedAreaNames.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Card per configurazione orari
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Seleziona i giorni lavorativi e imposta gli orari:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(giorni) { giorno ->
                    val nomeGiorno = giorno.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
                    val orarioGiorno = orariTemp[giorno]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = orarioGiorno != null,
                                onCheckedChange = { isChecked ->
                                    viewModel.onGiornoLavoroChanged(giorno, isChecked)
                                }
                            )

                            Text(
                                text = nomeGiorno.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (orarioGiorno != null) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        if (orarioGiorno != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp, top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = orarioGiorno.first.toString(),
                                    onValueChange = {
                                        viewModel.onOrarioInizioChanged(giorno, it)
                                    },
                                    label = { Text("Inizio") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                OutlinedTextField(
                                    value = orarioGiorno.second.toString(),
                                    onValueChange = {
                                        viewModel.onOrarioFineChanged(giorno, it)
                                    },
                                    label = { Text("Fine") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pulsanti di navigazione
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setStep(2) }) {
                Text("Indietro")
            }

            Button(
                onClick = {
                    // Salva gli orari per le aree selezionate
                    viewModel.salvaOrariSettimanali()
                    // Marca le aree come configurate
                    viewModel.marcaAreeConfigurateOrari(selectedAree)
                    // Pulisci la selezione corrente
                    viewModel.deselectAllAree()
                    // Torna al step 2 per permettere di configurare altre aree
                    viewModel.setStep(2)
                },
                enabled = orariTemp.isNotEmpty()
            ) {
                Text("Salva e Continua")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniFrequentiStep(viewModel: OnBoardingPianificaViewModel, onSetupComplete: () -> Unit,scaffoldVM: ScaffoldViewModel) {
    val pianificaState by viewModel.uiState.collectAsState()
    val turni = pianificaState.turni
    val nuovoTurno = pianificaState.nuovoTurno
    val checkTurniPronti = pianificaState.turniPronti
    val aree = pianificaState.aree

    val errorMsg = pianificaState.errorMsg
    val totalTurni = turni.size
    val maxTurni = 6
    val canAddTurno = totalTurni < maxTurni

    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()
    val azienda = userState.azienda
    val onDone = pianificaState.onDone

    LaunchedEffect(azienda) {
        if (!checkTurniPronti) {
            viewModel.generaTurniAi(azienda.nome)
        }
    }

    LaunchedEffect(onDone) {
        if (onDone) {
            scaffoldVM.onFullScreenChanged(false)
            viewModel.reset()
            onSetupComplete()
        }
    }

    if(errorMsg != null) {
        StatusDialog(
            message = errorMsg, onDismiss = { viewModel.clearError() },
            statusType = DialogStatusType.ERROR
        )
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
                    AiBanner(azienda.nome, checkTurniPronti)
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
                OutlinedButton(onClick = { viewModel.setStep(2) }) {
                    Text("Indietro")
                }

                Button(
                    onClick = {
                        viewModel.onComplete(azienda.idAzienda)
                        userViewModel.updateTurniAree(aree, turni)
                    },
                    enabled = totalTurni > 0
                ) {
                    Text("Completa Setup")
                }
            }
        }
    }
}