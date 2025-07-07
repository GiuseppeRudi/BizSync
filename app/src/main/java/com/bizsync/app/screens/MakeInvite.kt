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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.ui.components.ContractTypeSection
import com.bizsync.ui.components.DepartmentSelectionCard
import com.bizsync.ui.components.EmailField
import com.bizsync.ui.components.ManagerRoleSection
import com.bizsync.ui.components.SettoreAziendaleCard
import com.bizsync.ui.components.StepProgressIndicator
import com.bizsync.ui.components.WeeklyHoursSection
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.viewmodels.ManageInviteViewModel


@Composable
fun CreateInviteContent(
    inviteVM: ManageInviteViewModel,
    azienda: AziendaUi,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inviteState by inviteVM.uiState.collectAsState()
    val totalSteps = 2
    val currentStep = inviteState.currentStep

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp), // facoltativo per spazio extra sotto
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
            }
            Text(
                text = "Nuovo Invito",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        StepProgressIndicator(
            currentStep = currentStep,
            totalSteps = totalSteps,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when (currentStep) {
            1 -> {
                FirstStepContent(
                    inviteVM = inviteVM,
                    azienda = azienda,
                    onNextStep = { inviteVM.setCurrentStep(2) },
                    modifier = Modifier
                )
            }
            2 -> {
                SecondStepContent(
                    inviteVM = inviteVM,
                    onPreviousStep = { inviteVM.setCurrentStep(1) },
                    onComplete = {
                        inviteVM.inviaInvito(azienda)
                        onBack()
                    },
                    modifier = Modifier
                )
            }
        }
    }
}



@Composable
fun FirstStepContent(
    inviteVM: ManageInviteViewModel,
    azienda : AziendaUi,
    onNextStep: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by inviteVM.uiState.collectAsState()
    val isValid = inviteVM.isCurrentStepValid()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header del primo step
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Informazioni Base",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Inserisci i dati principali del nuovo dipendente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Campo Email
        EmailField(
            value =  uiState.invite.email, // inviteState.invite.email
            onValueChange = { inviteVM.onEmailChanged(it) },
            label = "Indirizzo Email",
            placeholder = "esempio@azienda.com",
            leadingIcon = Icons.Default.Email,
            isEmail = true
        )

        // Campo Posizione Lavorativa (ex nomeRuolo)
        EmailField(
            value =  uiState.invite.posizioneLavorativa, // inviteState.invite.posizioneLavorativa
            onValueChange = { inviteVM.setPosizioneLavorativa(it) },
            label = "Posizione Lavorativa",
            placeholder = "Es. Sviluppatore Frontend",
            leadingIcon = Icons.Default.Work
        )

        // Selezione Dipartimento/Area di Lavoro
        DepartmentSelectionCard(
            selectedDepartment = uiState.invite.dipartimento, // inviteState.invite.dipartimento
            departments = azienda.areeLavoro, // azienda.areeLavoro
            onDepartmentSelected = { inviteVM.setDipartimento(it) }
        )

        // Settore Aziendale (readonly)
        SettoreAziendaleCard(
            settore = azienda.sector
        )

        // Tipo Contratto
        ContractTypeSection(
            selectedContractType = uiState.invite.tipoContratto,
            onContractTypeSelected = { inviteVM.setTipoContratto(it) /* inviteVM.onTipoContrattoChanged(it) */ }
        )

        // Ore Settimanali
        WeeklyHoursSection(
            weeklyHours = uiState.invite.oreSettimanali, // inviteState.invite.oreSettimanali
            onWeeklyHoursChanged = { inviteVM.setOreSettimanali(it)/* inviteVM.onOreSettimanaliChanged(it) */ }
        )

        // Sezione ruolo manageriale
        ManagerRoleSection(
            isManager = uiState.invite.manager, // inviteState.invite.manager
            onManagerChanged = { inviteVM.onManagerChanged(it) /* inviteVM.onManagerChanged(it) */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNextStep,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = isValid
        ) {
            Text("Continua")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}@Composable
fun SecondStepContent(
    inviteVM: ManageInviteViewModel,
    onPreviousStep: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by inviteVM.uiState.collectAsState()

    LaunchedEffect(Unit) {
        // Auto-generate CCNL info when entering second step
        if (inviteVM.isCurrentStepValid() && uiState.ccnlnfo == Ccnlnfo()) {
            inviteVM.generateContractInfo()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header del secondo step
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Configurazione CCNL",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Verifica e modifica le informazioni contrattuali generate automaticamente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // AI Generation Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Informazioni CCNL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { inviteVM.generateContractInfo() },
                        enabled = !uiState.isLoading && inviteVM.isCurrentStepValid(),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rigenera", fontSize = 12.sp)
                        }
                    }
                }

                if (uiState.resultMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.resultMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // CCNL Info Form
        if (uiState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generazione informazioni CCNL...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Sostituiamo LazyColumn con Column normale per evitare problemi di vincoli infiniti
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.ccnlnfo.settore,
                        onValueChange = { inviteVM.updateCcnlSettore(it) },
                        label = { Text("Settore CCNL") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.ccnlnfo.ruolo,
                        onValueChange = { inviteVM.updateCcnlRuolo(it) },
                        label = { Text("Ruolo/Qualifica") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.ccnlnfo.ferieAnnue.toString(),
                        onValueChange = { inviteVM.updateCcnlFerieAnnue(it) },
                        label = { Text("Ferie Annue (giorni)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.ccnlnfo.rolAnnui.toString(),
                        onValueChange = { inviteVM.updateCcnlRolAnnui(it) },
                        label = { Text("ROL Annui (ore)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.ccnlnfo.stipendioAnnualeLordo.toString(),
                        onValueChange = { inviteVM.updateCcnlStipendio(it) },
                        label = { Text("Stipendio Annuale Lordo (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.ccnlnfo.malattiaRetribuita.toString(),
                        onValueChange = { inviteVM.updateCcnlMalattia(it) },
                        label = { Text("Malattia Retribuita (giorni)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pulsanti navigazione
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousStep,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Indietro")
            }

            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invia Invito")
            }
        }
    }
}

