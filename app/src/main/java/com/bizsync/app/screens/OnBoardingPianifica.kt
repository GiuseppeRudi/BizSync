package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.components.UniversalCard
import com.bizsync.ui.viewmodels.OnBoardingPianificaViewModel
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPianificaScreen(
    onSetupComplete: () -> Unit,
) {
    val viewModel: OnBoardingPianificaViewModel = hiltViewModel()
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
            0 -> WelcomeStep(viewModel)
            1 -> AreeLavoroStep(viewModel)
            2 -> SelezioneAreeOrariStep(viewModel)
            3 -> ConfigurazioneOrariStep(viewModel)
            4 -> TurniFrequentiStep(viewModel, onSetupComplete, scaffoldVM)
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
            onClick = { viewModel.setStep(1) },
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
    val azienda = userState.azienda

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
                            onDelete = { viewModel.onRimuoviAreaById(area.nomeArea) }
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
                            enabled = canAddArea && nuovaArea.nomeArea.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aggiungi",
                                tint = if (canAddArea && nuovaArea.nomeArea.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
                    Text("Configura Orari")
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
    val areeOrariConfigurati = pianificaState.areeOrariConfigurati

    // Calcola le aree configurate e non configurate
    val areeConfigurate = aree.filter { areeOrariConfigurati.contains(it.nomeArea) }
    val areeNonConfigurate = aree.filter { !areeOrariConfigurati.contains(it.nomeArea) }
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedAree.size} area/e selezionata/e per la configurazione orari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aree già configurate:",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(areeConfigurate) { area ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Configurata",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = area.nomeArea,
                                        modifier = Modifier.padding(start = 12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    if (area.orariSettimanali.isNotEmpty()) {
                                        val orariCount = area.orariSettimanali.size
                                        Text(
                                            text = "$orariCount giorni",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (areeNonConfigurate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (areeNonConfigurate.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aree da configurare:",
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectAllAreeNonConfigurate(areeNonConfigurate.map { it.nomeArea })
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.SelectAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Seleziona Tutte")
                        }

                        OutlinedButton(
                            onClick = { viewModel.deselectAllAree() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deseleziona")
                        }
                    }

                    LazyColumn {
                        items(areeNonConfigurate) { area ->
                            Card(
                                modifier = Modifier.padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedAree.contains(area.nomeArea))
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedAree.contains(area.nomeArea),
                                        onCheckedChange = { isChecked ->
                                            viewModel.onAreaSelectionChanged(area.nomeArea, isChecked)
                                        }
                                    )
                                    Text(
                                        text = area.nomeArea,
                                        modifier = Modifier.padding(start = 12.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (selectedAree.contains(area.nomeArea))
                                            FontWeight.Medium
                                        else
                                            FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TaskAlt,
                            contentDescription = "Completato",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Configurazione Completata!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Tutte le aree hanno gli orari configurati. Ora puoi procedere alla configurazione dei turni frequenti.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { viewModel.setStep(1) }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Indietro")
            }

            if (tutteAreeConfigurate) {
                Button(onClick = { viewModel.setStep(4) }) {
                    Text("Continua ai Turni")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.setStep(3) },
                    enabled = selectedAree.isNotEmpty()
                ) {
                    Text("Configura Orari")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigurazioneOrariStep(viewModel: OnBoardingPianificaViewModel) {
    val pianificaState by viewModel.uiState.collectAsState()
    val selectedAree = pianificaState.selectedAree
    val orariTemp = pianificaState.orariTemp

    val giorni = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

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
            text = "Imposta gli orari per: ${selectedAree.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Card con gli orari configurabili
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(giorni) { giorno ->
                    val nomeGiorno = giorno.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
                    val orarioGiorno = orariTemp[giorno]

                    GiornoOrarioItem(
                        nomeGiorno = nomeGiorno.replaceFirstChar { it.uppercase() },
                        orarioGiorno = orarioGiorno,
                        onGiornoLavoroChanged = { isLavorativo ->
                            viewModel.onGiornoLavoroChanged(giorno, isLavorativo)
                        },
                        onOrarioInizioChanged = { nuovoOrario ->
                            viewModel.onOrarioInizioChangedLocalTime(giorno, nuovoOrario)
                        },
                        onOrarioFineChanged = { nuovoOrario ->
                            viewModel.onOrarioFineChangedLocalTime(giorno, nuovoOrario)
                        }
                    )
                }
            }
        }

        // Pulsanti azione rapida
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.impostaOrariStandard() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Orari Standard")
            }

            OutlinedButton(
                onClick = { viewModel.resetOrariTemp() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
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

@Composable
fun GiornoOrarioItem(
    nomeGiorno: String,
    orarioGiorno: Pair<LocalTime, LocalTime>?,
    onGiornoLavoroChanged: (Boolean) -> Unit,
    onOrarioInizioChanged: (LocalTime) -> Unit,
    onOrarioFineChanged: (LocalTime) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (orarioGiorno != null)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = orarioGiorno != null,
                    onCheckedChange = onGiornoLavoroChanged
                )

                Text(
                    text = nomeGiorno,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (orarioGiorno != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (orarioGiorno != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            if (orarioGiorno != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Orario di inizio
                    TimePickerField(
                        value = orarioGiorno.first,
                        onValueChange = onOrarioInizioChanged,
                        label = "Inizio",
                        modifier = Modifier.weight(1f)
                    )

                    // Orario di fine
                    TimePickerField(
                        value = orarioGiorno.second,
                        onValueChange = onOrarioFineChanged,
                        label = "Fine",
                        modifier = Modifier.weight(1f)
                    )
                }

                val durata = java.time.Duration.between(orarioGiorno.first, orarioGiorno.second)
                val ore = durata.toHours()
                val minuti = durata.toMinutes() % 60

                Text(
                    text = "Durata: ${ore}h ${minuti}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniFrequentiStep(
    viewModel: OnBoardingPianificaViewModel,
    onSetupComplete: () -> Unit,
    scaffoldVM: ScaffoldViewModel
) {
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

    if (errorMsg != null) {
        StatusDialog(
            message = errorMsg,
            onDismiss = { viewModel.clearError() },
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