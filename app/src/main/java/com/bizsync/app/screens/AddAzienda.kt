package com.bizsync.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.DipendentiSelector
import com.bizsync.ui.components.GiornoPublicazioneSelector
import com.bizsync.ui.components.SettoreSelector
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.viewmodels.AddAziendaViewModel

@Composable
fun AddAzienda(onTerminate: () -> Unit) {
    val addaziendaviewmodel: AddAziendaViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current

    val userState by userviewmodel.uiState.collectAsState()
    val addAziendaState by addaziendaviewmodel.uiState.collectAsState()
    val currentStep = addAziendaState.currentStep
    val uid = userState.user.uid
    val isAgencyUpdateAddAzienda = addAziendaState.isAgencyAdded
    val isAgencyAddedUser = userState.hasLoadedAgency

    LaunchedEffect(isAgencyUpdateAddAzienda, isAgencyAddedUser) {
        if (isAgencyUpdateAddAzienda) {
            userviewmodel.onAddAziendaRole(addAziendaState.azienda.idAzienda)
        }
        if (isAgencyAddedUser && isAgencyUpdateAddAzienda) {
            onTerminate()
        }
    }


    if (addAziendaState.resultMsg != null || userState.resultMsg != null) {
        StatusDialog(
            message = "Caricamento completato con successo",
            DialogStatusType.SUCCESS,
            onDismiss = {
                addaziendaviewmodel.clearMessage()
                userviewmodel.clearMessage()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con titolo e progress
        StepHeader(currentStep = currentStep, totalSteps = 4)

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                1 -> StepOne(addaziendaviewmodel)
                2 -> StepTwo(addaziendaviewmodel)
                3 -> StepThree(addaziendaviewmodel)
                4 -> StepFour(addaziendaviewmodel)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pulsanti di navigazione
        NavigationButtons(
            currentStep = currentStep,
            totalSteps = 4,
            onPrevious = { addaziendaviewmodel.onCurrentStepDown() },
            onNext = { addaziendaviewmodel.onCurrentStepUp() },
            onComplete = { addaziendaviewmodel.aggiungiAzienda(uid) }
        )
    }
}

@Composable
private fun StepHeader(currentStep: Int, totalSteps: Int) {
    Column {
        Text(
            text = "Configura la tua azienda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Passo $currentStep di $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StepContainer(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@Composable
private fun StepOne(addaziendaviewmodel: AddAziendaViewModel) {
    val addAziendaState by addaziendaviewmodel.uiState.collectAsState()
    val nomeAzienda = addAziendaState.azienda.nome

    StepContainer(
        icon = Icons.Default.Business,
        title = "Nome dell'azienda",
        subtitle = "Iniziamo con le informazioni di base"
    ) {
        OutlinedTextField(
            value = nomeAzienda,
            onValueChange = { addaziendaviewmodel.onNomeAziendaChanged(it) },
            label = { Text("Nome azienda") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun StepTwo(addaziendaviewmodel: AddAziendaViewModel) {
    StepContainer(
        icon = Icons.Default.Group,
        title = "Numero di dipendenti",
        subtitle = "Seleziona il range che meglio descrive la tua azienda"
    ) {
        DipendentiSelector(addaziendaviewmodel)
    }
}

@Composable
private fun StepThree(addaziendaviewmodel: AddAziendaViewModel) {
    StepContainer(
        icon = Icons.Default.Settings,
        title = "Settore di attività",
        subtitle = "In che settore opera la tua azienda?"
    ) {
        SettoreSelector(addaziendaviewmodel)
    }
}

@Composable
private fun StepFour(addaziendaviewmodel: AddAziendaViewModel) {
    StepContainer(
        icon = Icons.Default.Schedule,
        title = "Pubblicazione turni",
        subtitle = "Configura quando pubblichi i turni settimanali"
    ) {
        GiornoPublicazioneSelector(addaziendaviewmodel)
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onPrevious,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Indietro")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (currentStep < totalSteps) {
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Avanti")
            }
        } else {
            Button(
                onClick = onComplete,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Completa configurazione")
            }
        }
    }
}