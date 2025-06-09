package com.bizsync.app.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

data class AreaLavoro(
    val id: String = "",
    val nome: String = "",
    val descrizione: String = ""
)

data class TurnoFrequente(
    val id: String = "",
    val nome: String = "",
    val oraInizio: String = "",
    val oraFine: String = "",
    val descrizione: String = ""
)


@Preview
@Composable
fun setupPreview()
{
    SetupTutorialScreen(onSetupComplete = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupTutorialScreen(
    onSetupComplete: () -> Unit,
) {

    val areeDefault = listOf(
        "Reception",
        "Cucina",
        "Sala",
        "Bar",
        "Magazzino",
        "Pulizie"
    )

    val turniDefault = listOf(
        TurnoFrequente("", "Mattina", "08:00", "14:00", "Turno mattutino"),
        TurnoFrequente("", "Pomeriggio", "14:00", "20:00", "Turno pomeridiano"),
        TurnoFrequente("", "Sera", "20:00", "02:00", "Turno serale"),
        TurnoFrequente("", "Notte", "22:00", "06:00", "Turno notturno")
    )

    var currentStep by remember { mutableIntStateOf(0) }
    var areeSelezionate by remember { mutableStateOf(mutableSetOf<String>()) }
    var areePersonalizzate by remember { mutableStateOf(mutableListOf<String>()) }
    var turniFrequenti by remember { mutableStateOf(turniDefault.toMutableList()) }
    var nuovaArea by remember { mutableStateOf("") }
    var nuovoTurno by remember { mutableStateOf(TurnoFrequente()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        when (currentStep) {
            0 -> {
                // Step 1: Benvenuto e spiegazione
                WelcomeStep(onNext = { currentStep = 1 })
            }
            1 -> {
                // Step 2: Configurazione Aree di Lavoro
                AreeLavoroStep(
                    areeDefault = areeDefault,
                    areeSelezionate = areeSelezionate,
                    areePersonalizzate = areePersonalizzate,
                    nuovaArea = nuovaArea,
                    onAreaToggle = { area ->
                        if (areeSelezionate.contains(area)) {
                            areeSelezionate.remove(area)
                        } else {
                            areeSelezionate.add(area)
                        }
                    },
                    onNuovaAreaChange = { nuovaArea = it },
                    onAggiungiArea = {
                        if (nuovaArea.isNotBlank()) {
                            areePersonalizzate.add(nuovaArea)
                            nuovaArea = ""
                        }
                    },
                    onRimuoviArea = { area ->
                        areePersonalizzate.remove(area)
                    },
                    onNext = {
                        if (areeSelezionate.isNotEmpty() || areePersonalizzate.isNotEmpty()) {
                            currentStep = 2
                        }
                    },
                    onBack = { currentStep = 0 }
                )
            }
            2 -> {
                // Step 3: Configurazione Turni Frequenti
                TurniFrequentiStep(
                    turniFrequenti = turniFrequenti,
                    nuovoTurno = nuovoTurno,
                    onTurnoChange = { nuovoTurno = it },
                    onAggiungiTurno = {
                        if (nuovoTurno.nome.isNotBlank() &&
                            nuovoTurno.oraInizio.isNotBlank() &&
                            nuovoTurno.oraFine.isNotBlank()) {
                            turniFrequenti.add(nuovoTurno)
                            nuovoTurno = TurnoFrequente()
                        }
                    },
                    onRimuoviTurno = { turno ->
                        turniFrequenti.remove(turno)
                    },
                    onComplete = {
                        // Salva i dati e completa il setup
                        // setupViewModel.salvaConfigurazioneIniziale(
//                            aree = (areeSelezionate + areePersonalizzate).toList(),
//                            turni = turniFrequenti
//                        )
                        onSetupComplete()
                    },
                    onBack = { currentStep = 1 }
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
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
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inizia Configurazione")
        }
    }
}

@Composable
fun AreeLavoroStep(
    areeDefault: List<String>,
    areeSelezionate: MutableSet<String>,
    areePersonalizzate: MutableList<String>,
    nuovaArea: String,
    onAreaToggle: (String) -> Unit,
    onNuovaAreaChange: (String) -> Unit,
    onAggiungiArea: () -> Unit,
    onRimuoviArea: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Configura le Aree di Lavoro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Seleziona le aree predefinite o aggiungi le tue personalizzate",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
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

            items(areeDefault) { area ->
                FilterChip(
                    onClick = { onAreaToggle(area) },
                    label = { Text(area) },
                    selected = areeSelezionate.contains(area),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
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
                        value = nuovaArea,
                        onValueChange = onNuovaAreaChange,
                        label = { Text("Nome area") },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onAggiungiArea) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi")
                    }
                }
            }

            if (areePersonalizzate.isNotEmpty()) {
                item {
                    Text(
                        text = "Aree Personalizzate:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(areePersonalizzate) { area ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(area)
                            IconButton(onClick = { onRimuoviArea(area) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Indietro")
            }

            Button(
                onClick = onNext,
                enabled = areeSelezionate.isNotEmpty() || areePersonalizzate.isNotEmpty()
            ) {
                Text("Continua")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniFrequentiStep(
    turniFrequenti: MutableList<TurnoFrequente>,
    nuovoTurno: TurnoFrequente,
    onTurnoChange: (TurnoFrequente) -> Unit,
    onAggiungiTurno: () -> Unit,
    onRimuoviTurno: (TurnoFrequente) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Configura i Turni Frequenti",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "I turni qui configurati saranno suggeriti durante la creazione",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(turniFrequenti) { turno ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
                                text = turno.nome,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${turno.oraInizio} - ${turno.oraFine}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (turno.descrizione.isNotBlank()) {
                                Text(
                                    text = turno.descrizione,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onRimuoviTurno(turno) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                        }
                    }
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
                            onValueChange = { onTurnoChange(nuovoTurno.copy(nome = it)) },
                            label = { Text("Nome turno") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nuovoTurno.oraInizio,
                                onValueChange = { onTurnoChange(nuovoTurno.copy(oraInizio = it)) },
                                label = { Text("Ora inizio (HH:MM)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = nuovoTurno.oraFine,
                                onValueChange = { onTurnoChange(nuovoTurno.copy(oraFine = it)) },
                                label = { Text("Ora fine (HH:MM)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nuovoTurno.descrizione,
                            onValueChange = { onTurnoChange(nuovoTurno.copy(descrizione = it)) },
                            label = { Text("Descrizione (opzionale)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onAggiungiTurno,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nuovoTurno.nome.isNotBlank() &&
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
            OutlinedButton(onClick = onBack) {
                Text("Indietro")
            }

            Button(
                onClick = onComplete,
                enabled = turniFrequenti.isNotEmpty()
            ) {
                Text("Completa Setup")
            }
        }
    }
}