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
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.ShiftCard
import com.bizsync.ui.viewmodels.OnBoardingPianificaViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupTutorialScreen(
    onSetupComplete: () -> Unit,
) {

    val userViewModel = LocalUserViewModel.current
    val azienda by userViewModel.azienda.collectAsState()
    val viewmodel : OnBoardingPianificaViewModel = hiltViewModel()
    val currentStep by viewmodel.currentStep.collectAsState()

    LaunchedEffect(azienda) {
        viewmodel.generaTurniAi(azienda.Nome)
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
            0 -> { WelcomeStep(viewmodel) }
            1 -> { AreeLavoroStep(viewmodel) }
            2 -> { TurniFrequentiStep(viewmodel) }
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
    val checkAreeDefualt by viewModel.listaPronta.collectAsState()

    // Calcolo del numero totale di aree
    val totalAree = areeDefaultAttive.size
    val maxAree = 10
    val canAddArea = totalAree < maxAree


    Column(
        modifier = Modifier.fillMaxSize()
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

        // Indicatore numero aree
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

            if (!checkAreeDefualt)
            {
                items(10){ item ->
                    ShiftCard(loading = true)
                }
            }

            items(areeDefaultAttive) { area ->
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
                        Text(
                            text = area.nomeArea,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(
                            onClick = { viewModel.onRimuoviAreaById(area.id) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Rimuovi $area",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
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
                        enabled = totalAree < maxAree,
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
            OutlinedButton(onClick = { viewModel.setStep(0)}) {
                Text("Indietro")
            }

            Button(
                onClick = {
                    // Qui puoi salvare le aree selezionate nel ViewModel
                    // viewModel.salvaAreeSelezionate(areeDefaultAttive + areePersonalizzate)
                    viewModel.setStep(2)
                },
                enabled = totalAree > 0
            ) {
                Text("Continua")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniFrequentiStep(viewModel: OnBoardingPianificaViewModel) {

}