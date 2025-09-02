package com.bizsync.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.components.DettagliGiornalieri
import com.bizsync.ui.components.TurniHeaderCard
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.PianificaEmployeeViewModel
import com.bizsync.ui.viewmodels.PianificaViewModel
import java.time.format.DateTimeFormatter


@Composable
fun PianificaDipendentiScreen(
    pianificaVM: PianificaViewModel
) {
    val scaffoldVM = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current

    val userState by userVM.uiState.collectAsState()
    val pianificaState by pianificaVM.uistate.collectAsState()

    val employeeVM: PianificaEmployeeViewModel = hiltViewModel()
    val employeeState by employeeVM.uiState.collectAsState()

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(false)
    }

    val selectionData = pianificaState.selectionData
    val weeklyisIdentical = pianificaState.weeklyisIdentical
    val weeklyShiftRiferimento = pianificaState.weeklyShiftRiferimento
    val weeklyShiftAttuale = pianificaState.weeklyShiftAttuale
    val dipartimento = employeeState.dipartimentoEmployee


    LaunchedEffect(weeklyShiftAttuale) {
        if(weeklyShiftAttuale != null)
        {
            val dipartimenti = weeklyShiftAttuale.dipartimentiAttivi
            val dipendenti = weeklyShiftAttuale.dipendentiAttivi
            val dipendentePassato = dipendenti.find { it.uid == userState.user.uid }
            if(dipendentePassato!=null)
            {
                val dipartimento = dipartimenti.find { it.nomeArea == dipendentePassato.dipartimento}

                if(dipartimento != null)
                    employeeVM.inizializzaDatiEmployee(userState.user.uid,  dipartimento)
            }
        }

    }

    LaunchedEffect(Unit) {
        employeeVM.inizializzaContratto( userState.contratto)

        if(weeklyShiftRiferimento != null)
        {
            val dipartimenti = weeklyShiftRiferimento.dipartimentiAttivi
            val dipendenti = weeklyShiftRiferimento.dipendentiAttivi
            val dipendentePassato = dipendenti.find { it.uid == userState.user.uid }
            if(dipendentePassato!=null)
            {
                val dipartimento = dipartimenti.find { it.nomeArea == dipendentePassato.dipartimento}

                if(dipartimento != null)
                    employeeVM.inizializzaDatiEmployee(userState.user.uid,  dipartimento)
            }
        }

    }

    LaunchedEffect(weeklyShiftRiferimento, weeklyShiftAttuale) {
        if(weeklyShiftRiferimento != null && weeklyShiftAttuale != null && weeklyShiftAttuale == weeklyShiftRiferimento) {
            pianificaVM.setWeeklyShiftIdentical(true)
        } else {
            pianificaVM.setWeeklyShiftIdentical(false)
        }

        if (weeklyShiftAttuale != null && !weeklyisIdentical ) {
            employeeVM.setTurniSettimanaliDipendente(weeklyShiftAttuale.weekStart, userState.azienda.idAzienda, userState.user.uid)
        }

        if(weeklyShiftRiferimento != null) {

            val weekStart = weeklyShiftRiferimento.weekStart
            employeeVM.setTurniSettimanaliDipendente(weekStart, userState.azienda.idAzienda, userState.user.uid)
        }
    }


    LaunchedEffect(selectionData) {
        pianificaVM.backToMain()

        if(selectionData != null) {
            pianificaVM.getWeeklyShiftCorrente(selectionData,userState.user.idAzienda)

            employeeVM.setTurniGiornalieri(selectionData)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        TurniHeaderCard(
            statistiche = employeeState.statisticheSettimanali,
            weeklyShift = weeklyShiftAttuale
        )


        Spacer(modifier = Modifier.height(16.dp))

        Calendar(pianificaVM)

        Spacer(modifier = Modifier.height(16.dp))

        // Area principale per i turni del dipendente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            if (employeeState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (employeeState.dettagliGiornalieri != null && employeeState.hasTurniGiornalieri) {
                DettagliGiornalieri(
                    dettagli = employeeState.dettagliGiornalieri!!,
                    turni = employeeState.turniGiornalieri,
                    colleghi = employeeState.colleghiTurno
                )
            } else if (selectionData != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Nessun turno assegnato",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Non hai turni assegnati per ${
                                selectionData.format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                )
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        // Mostra informazioni dipartimento anche quando non ci sono turni
                        if (dipartimento != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.7f
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Dipartimento: ${dipartimento.nomeArea}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val dayOfWeek = selectionData.dayOfWeek
                                    val orariDipartimento = dipartimento.orariSettimanali[dayOfWeek]

                                    if (orariDipartimento != null) {
                                        Text(
                                            text = "Orari: ${
                                                orariDipartimento.first.format(
                                                    DateTimeFormatter.ofPattern("HH:mm")
                                                )
                                            } - ${
                                                orariDipartimento.second.format(
                                                    DateTimeFormatter.ofPattern(
                                                        "HH:mm"
                                                    )
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "Dipartimento chiuso",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Seleziona una data dal calendario",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Potrai visualizzare i tuoi turni assegnati",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

}
