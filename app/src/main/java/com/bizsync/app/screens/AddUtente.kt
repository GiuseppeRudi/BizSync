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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.StatusDialog
import com.bizsync.ui.viewmodels.AddUtenteViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AddUtente(onChooseAzienda: () -> Unit) {
    val addutenteviewmodel: AddUtenteViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current

    val uiState by addutenteviewmodel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val isUserAdded = uiState.isUserAdded

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            addutenteviewmodel.onEmailChanged(user.email ?: "")
            addutenteviewmodel.onUidChanged(user.uid)
            addutenteviewmodel.onPhotoUrlChanged(user.photoUrl?.toString() ?: "")
        }
    }

    LaunchedEffect(isUserAdded) {
        if (isUserAdded) {
            onChooseAzienda()
        }
    }

    uiState.error?.let { error ->
        StatusDialog(
            message = error,
            onDismiss = { addutenteviewmodel.setErrore(null) },
            statusType = DialogStatusType.ERROR,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentStep) {
                    1 -> StepOne(addutenteviewmodel)
                    2 -> StepTwo(addutenteviewmodel)
                    3 -> StepThree(addutenteviewmodel)
                    4 -> StepFour(addutenteviewmodel)
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creazione profilo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pulsanti di navigazione
        NavigationButtons(
            currentStep = currentStep,
            totalSteps = 4,
            canProceed = addutenteviewmodel.canProceedToNextStep(currentStep),
            onPrevious = { addutenteviewmodel.onCurrentStepDown() },
            onNext = { addutenteviewmodel.onCurrentStepUp() },
            onComplete = { addutenteviewmodel.addUserAndPropaga(userviewmodel) },
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun StepHeader(currentStep: Int, totalSteps: Int) {
    Column {
        Text(
            text = "Configura il tuo profilo",
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
        progress = { currentStep.toFloat() / totalSteps.toFloat() },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = ProgressIndicatorDefaults.linearTrackColor,
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
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
private fun StepOne(addutenteviewmodel: AddUtenteViewModel) {
    val uiState by addutenteviewmodel.uiState.collectAsState()
    val userState = uiState.userState

    StepContainer(
        icon = Icons.Default.Person,
        title = "Dati personali",
        subtitle = "Iniziamo con nome e cognome"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = userState.nome,
                onValueChange = { addutenteviewmodel.onNomeChanged(it) },
                label = { Text("Nome *") },
                placeholder = { Text("Es: Mario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = userState.nome.isBlank()
            )

            OutlinedTextField(
                value = userState.cognome,
                onValueChange = { addutenteviewmodel.onCognomeChanged(it) },
                label = { Text("Cognome *") },
                placeholder = { Text("Es: Rossi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = userState.cognome.isBlank()
            )

            OutlinedTextField(
                value = userState.email,
                onValueChange = { }, // Read-only
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            Text(
                text = "💡 L'email viene presa automaticamente dal tuo account Google",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun StepTwo(addutenteviewmodel: AddUtenteViewModel) {
    val uiState by addutenteviewmodel.uiState.collectAsState()
    val userState = uiState.userState

    StepContainer(
        icon = Icons.Default.ContactMail,
        title = "Informazioni di contatto",
        subtitle = "Come possiamo raggiungerti"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = userState.numeroTelefono,
                onValueChange = { addutenteviewmodel.onNumeroTelefonoChanged(it) },
                label = { Text("Numero di telefono") },
                placeholder = { Text("Es: +39 123 456 7890") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = userState.indirizzo,
                onValueChange = { addutenteviewmodel.onIndirizzoChanged(it) },
                label = { Text("Indirizzo di residenza") },
                placeholder = { Text("Es: Via Roma 123, Milano") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Text(
                text = "ℹ️ Questi dati sono opzionali ma utili per le comunicazioni aziendali",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun StepThree(addutenteviewmodel: AddUtenteViewModel) {
    val uiState by addutenteviewmodel.uiState.collectAsState()
    val userState = uiState.userState

    StepContainer(
        icon = Icons.Default.Badge,
        title = "Informazioni anagrafiche",
        subtitle = "Dati per documenti e contratti"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = userState.codiceFiscale,
                onValueChange = { addutenteviewmodel.onCodiceFiscaleChanged(it.uppercase()) },
                label = { Text("Codice Fiscale") },
                placeholder = { Text("Es: RSSMRA80A01H501U") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = userState.dataNascita,
                onValueChange = { addutenteviewmodel.onDataNascitaChanged(it) },
                label = { Text("Data di nascita") },
                placeholder = { Text("Es: 01/01/1990") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = userState.luogoNascita,
                onValueChange = { addutenteviewmodel.onLuogoNascitaChanged(it) },
                label = { Text("Luogo di nascita") },
                placeholder = { Text("Es: Milano (MI)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "📄 Questi dati sono opzionali ma potrebbero essere richiesti per la gestione HR",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun StepFour(addutenteviewmodel: AddUtenteViewModel) {
    val uiState by addutenteviewmodel.uiState.collectAsState()
    val userState = uiState.userState

    StepContainer(
        icon = Icons.Default.Person,
        title = "Riepilogo profilo",
        subtitle = "Verifica i dati inseriti"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📝 Riepilogo dati",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ProfileSummaryItem("Nome", "${userState.nome} ${userState.cognome}")
                    ProfileSummaryItem("Email", userState.email)

                    if (userState.numeroTelefono.isNotBlank()) {
                        ProfileSummaryItem("Telefono", userState.numeroTelefono)
                    }

                    if (userState.indirizzo.isNotBlank()) {
                        ProfileSummaryItem("Indirizzo", userState.indirizzo)
                    }

                    if (userState.codiceFiscale.isNotBlank()) {
                        ProfileSummaryItem("Codice Fiscale", userState.codiceFiscale)
                    }

                    if (userState.dataNascita.isNotBlank()) {
                        ProfileSummaryItem("Data di nascita", userState.dataNascita)
                    }

                    if (userState.luogoNascita.isNotBlank()) {
                        ProfileSummaryItem("Luogo di nascita", userState.luogoNascita)
                    }
                }
            }

            Text(
                text = "✨ Perfetto! Il tuo profilo è pronto. Nel prossimo passaggio potrai scegliere un'azienda esistente o crearne una nuova.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                text = "🔒 La posizione lavorativa, il dipartimento e i ruoli verranno assegnati successivamente dal manager dell'azienda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileSummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isLoading,
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
                enabled = canProceed && !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text("Avanti")
            }
        } else {
            Button(
                onClick = onComplete,
                enabled = canProceed && !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crea profilo")
                }
            }
        }
    }
}