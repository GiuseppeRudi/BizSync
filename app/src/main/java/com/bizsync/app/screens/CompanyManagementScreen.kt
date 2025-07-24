package com.bizsync.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.viewmodels.CompanyViewModel
import com.bizsync.ui.viewmodels.PianificaViewModel
import kotlinx.coroutines.flow.update
import com.bizsync.domain.constants.enumClass.CompanyOperation
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun CompanyManagementScreen(onBackClick: () -> Unit) {
    val companyVm: CompanyViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current
    val userState by userviewmodel.uiState.collectAsState()
    val azienda = userState.azienda
    val companyState by companyVm.uiState.collectAsState()
    val onBoardingDone = companyState.onBoardingDone

    LaunchedEffect(Unit) {
        companyVm.checkOnBoardingStatus(azienda)
    }

    when (onBoardingDone) {
        null -> CircularProgressIndicator()
        false -> SetupPianificaScreen(onSetupComplete = { companyVm.setOnBoardingDone(true) })
        true -> CompanyCore(companyVm, azienda, onBackClick)
    }
}

@Composable
fun CompanyCore(
    companyVm: CompanyViewModel,
    azienda: AziendaUi,
    onBackClick: () -> Unit
) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()
    val companyState by companyVm.uiState.collectAsState()
    val selectedOperation = companyState.selectedOperation
    val areeLavoro: List<AreaLavoro> = azienda.areeLavoro
    val orariSettimanali: Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>> =
        buildOrariSettimanaliMap(areeLavoro)

    when (selectedOperation) {
        CompanyOperation.DETTAGLI_AZIENDA -> {
            DettagliAziendaScreen(
                azienda = azienda,
                onBackClick = { companyVm.setSelectedOperation(null) },
                onSaveChanges = { updatedAzienda ->
                    // Logica per salvare le modifiche
//                    companyVm.updateAziendaDetails(updatedAzienda)
                    companyVm.setSelectedOperation(null)
                },
                isLoading = companyState.isLoading
            )
        }

        CompanyOperation.DIPARTIMENTI -> {
            DipartimentiManagementScreen(companyVm, areeLavoro, azienda.idAzienda, orariSettimanali)
        }

        CompanyOperation.GESTIONE_INVITI -> {
            InviteManagementScreen(companyVm)
        }

        CompanyOperation.TURNI_FREQUENTI -> {
            TurniFrequentiManagementScreen(
                currentTurni = azienda.turniFrequenti,
                onBackClick = { companyVm.setSelectedOperation(null) },
                onSaveChanges = { nuoviTurni ->
                    companyVm.setSelectedOperation(null)
                },
                isLoading = companyState.isLoading
            )
        }

        CompanyOperation.GIORNO_PUBBLICAZIONE -> {
            GiornoPublicazioneManagementScreen(
                companyVm = companyVm,
                userVm = userViewModel,
                idAzienda = azienda.idAzienda
            )
        }

        null -> OperationSelectorScreen(
            azienda = azienda,
            onOperationSelected = { operation ->
                companyVm.setSelectedOperation(operation)
            },
            onBackClick = onBackClick
        )
    }
}

@Composable
fun OperationSelectorScreen(
    azienda: AziendaUi,
    onOperationSelected: (CompanyOperation) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
            }
            Text(
                text = "Gestione Azienda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(48.dp))
        }




        // Lista Operazioni
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {


            item {
                RiepilogoAziendaCard(azienda = azienda)
            }

            item {
                OperationCard(
                    title = "Dettagli Azienda",
                    description = "Modifica informazioni, settore e geolocalizzazione",
                    icon = Icons.Default.Business,
                    onClick = { onOperationSelected(CompanyOperation.DETTAGLI_AZIENDA) }
                )
            }

            item {
                OperationCard(
                    title = "Dipartimenti",
                    description = "Gestisci aree di lavoro e orari",
                    icon = Icons.Default.Domain,
                    onClick = { onOperationSelected(CompanyOperation.DIPARTIMENTI) }
                )
            }

            item {
                OperationCard(
                    title = "Turni Frequenti",
                    description = "Configura i turni ricorrenti",
                    icon = Icons.Default.Schedule,
                    onClick = { onOperationSelected(CompanyOperation.TURNI_FREQUENTI) }
                )
            }

            item {
                OperationCard(
                    title = "Gestione Inviti",
                    description = "Invita nuovi dipendenti",
                    icon = Icons.Default.PersonAdd,
                    onClick = { onOperationSelected(CompanyOperation.GESTIONE_INVITI) }
                )
            }

            item {
                OperationCard(
                    title = "Giorno Pubblicazione",
                    description = "Imposta quando pubblicare i turni",
                    icon = Icons.Default.CalendarToday,
                    onClick = { onOperationSelected(CompanyOperation.GIORNO_PUBBLICAZIONE) }
                )
            }
        }
    }
}

@Composable
fun RiepilogoAziendaCard(
    azienda: AziendaUi, ) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riepilogo Azienda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome Azienda
            InfoRow(
                label = "Nome",
                value = azienda.nome.ifEmpty { "Non specificato" },
                icon = Icons.Default.Business
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Settore
            InfoRow(
                label = "Settore",
                value = azienda.sector.ifEmpty { "Non specificato" },
                icon = Icons.Default.Category
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Numero Dipendenti
            InfoRow(
                label = "Dipendenti",
                value = azienda.numDipendentiRange.ifEmpty { "Non specificato" },
                icon = Icons.Default.People
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Geolocalizzazione
            InfoRow(
                label = "Posizione",
                value = if (azienda.latitudine != 0.0 && azienda.longitudine != 0.0) {
                    "Configurata (${azienda.tolleranzaMetri}m tolleranza)"
                } else {
                    "Non configurata"
                },
                icon = Icons.Default.LocationOn
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Aree di Lavoro
            InfoRow(
                label = "Aree di Lavoro",
                value = "${azienda.areeLavoro.size} configurate",
                icon = Icons.Default.Domain
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OperationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Vai",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DettagliAziendaScreen(
    azienda: AziendaUi,
    onBackClick: () -> Unit,
    onSaveChanges: (AziendaUi) -> Unit,
    isLoading: Boolean
) {
    var nome by remember { mutableStateOf(azienda.nome) }
    var settore by remember { mutableStateOf(azienda.sector) }
    var numDipendenti by remember { mutableStateOf(azienda.numDipendentiRange) }
    var latitudine by remember { mutableStateOf(azienda.latitudine.toString()) }
    var longitudine by remember { mutableStateOf(azienda.longitudine.toString()) }
    var tolleranza by remember { mutableStateOf(azienda.tolleranzaMetri.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
            }
            Text(
                text = "Dettagli Azienda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    val updatedAzienda = azienda.copy(
                        nome = nome,
                        sector = settore,
                        numDipendentiRange = numDipendenti,
                        latitudine = latitudine.toDoubleOrNull() ?: 0.0,
                        longitudine = longitudine.toDoubleOrNull() ?: 0.0,
                        tolleranzaMetri = tolleranza.toIntOrNull() ?: 100
                    )
                    onSaveChanges(updatedAzienda)
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Salva")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Azienda") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null)
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = settore,
                    onValueChange = { settore = it },
                    label = { Text("Settore") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = numDipendenti,
                    onValueChange = { numDipendenti = it },
                    label = { Text("Range Numero Dipendenti") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.People, contentDescription = null)
                    },
                    placeholder = { Text("es. 10-50, 50-100, 100+") }
                )
            }

            item {
                Text(
                    text = "Geolocalizzazione",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitudine,
                        onValueChange = { latitudine = it },
                        label = { Text("Latitudine") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        }
                    )
                    OutlinedTextField(
                        value = longitudine,
                        onValueChange = { longitudine = it },
                        label = { Text("Longitudine") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = tolleranza,
                    onValueChange = { tolleranza = it },
                    label = { Text("Tolleranza (metri)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Radar, contentDescription = null)
                    },
                    placeholder = { Text("100") }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Informazioni Geolocalizzazione",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "La geolocalizzazione permette di verificare che i dipendenti si trovino nell'area di lavoro quando timbrano. La tolleranza indica il raggio in metri entro cui è valida la posizione.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

fun buildOrariSettimanaliMap(aree: List<AreaLavoro>): Map<String, Map<DayOfWeek, Pair<LocalTime, LocalTime>>> {
    return aree.associate { area ->
        area.nomeArea to area.orariSettimanali
    }
}