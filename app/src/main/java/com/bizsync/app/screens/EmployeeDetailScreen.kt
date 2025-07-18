package com.bizsync.app.screens

import EmployeeAvatar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.viewmodels.EmployeeManagementViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employee: UserUi,
    employeeVm : EmployeeManagementViewModel,
) {
    var showFireDialog by remember { mutableStateOf(false) }

    val uiState by employeeVm.uiState.collectAsState()

    val currentSection = uiState.currentSection
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        // Header con titolo dinamico
        TopAppBar(
            title = {
                Text(
                    text = when (currentSection) {
                        EmployeeSection.MAIN -> "Dettagli Dipendente"
                        EmployeeSection.CONTRACT -> "Contratto e CCNL"
                        EmployeeSection.PAST_SHIFTS -> "Turni Passati"
                        EmployeeSection.FUTURE_SHIFTS -> "Turni Futuri"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (currentSection == EmployeeSection.MAIN) {
                            employeeVm.updateSelectedEmployee(null)
                        } else {
                            employeeVm.setCurrentSection(EmployeeSection.MAIN)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        )

        // Contenuto che cambia in base alla sezione
        when (currentSection) {
            EmployeeSection.MAIN -> {
                MainSection(
                    employee = employee,
                    onSectionClick = { section ->    employeeVm.setCurrentSection(section) },
                    onFireEmployeeClick = { showFireDialog = true }
                )
            }
            EmployeeSection.CONTRACT -> {
                ContractDetailScreen(employee = employee, employeeVm )
            }
            EmployeeSection.PAST_SHIFTS -> {
                ShiftsScreen(
                    employee = employee,
                    employeeVm,
                    true,
                )
            }
            EmployeeSection.FUTURE_SHIFTS -> {
                ShiftsScreen(
                    employee = employee,
                    employeeVm,
                    false,
                )            }
        }
    }

    // Dialog conferma licenziamento
    if (showFireDialog) {
        AlertDialog(
            onDismissRequest = { showFireDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE74C3C)
                )
            },
            title = {
                Text("Conferma Licenziamento")
            },
            text = {
                Text("Sei sicuro di voler licenziare ${employee.nome} ${employee.cognome}? Questa azione non può essere annullata.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFireDialog = false
                        // Implementa logica di licenziamento
                    }
                ) {
                    Text("Conferma", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFireDialog = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}
@Composable
fun MainSection(
    employee: UserUi,
    onSectionClick: (EmployeeSection) -> Unit,
    onFireEmployeeClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profilo dipendente
        item {
            EmployeeProfileCard(employee)
        }

        // Sezioni disponibili
        item {
            Text(
                text = "Gestione",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Contratto e CCNL
        item {
            SectionCard(
                title = "Contratto e CCNL",
                subtitle = "Visualizza dettagli contrattuali e CCNL",
                icon = Icons.Default.Description,
                iconColor = Color(0xFF3498DB),
                onClick = { onSectionClick(EmployeeSection.CONTRACT) }
            )
        }

        // Turni passati
        item {
            SectionCard(
                title = "Turni Passati",
                subtitle = "Visualizza lo storico dei turni lavorativi",
                icon = Icons.Default.History,
                iconColor = Color(0xFF2ECC71),
                onClick = { onSectionClick(EmployeeSection.PAST_SHIFTS) }
            )
        }

        // Turni futuri
        item {
            SectionCard(
                title = "Turni Futuri",
                subtitle = "Visualizza i turni programmati",
                icon = Icons.Default.Schedule,
                iconColor = Color(0xFFF39C12),
                onClick = { onSectionClick(EmployeeSection.FUTURE_SHIFTS) }
            )
        }

        // Sezione azioni
        item {
            Text(
                text = "Azioni",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Licenziamento
        item {
            SectionCard(
                title = "Licenziamento",
                subtitle = "Rimuovi il dipendente dall'azienda",
                icon = Icons.Default.Delete,
                iconColor = Color(0xFFE74C3C),
                onClick = onFireEmployeeClick
            )
        }
    }
}


@Composable
fun EmployeeProfileCard(employee: UserUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            EmployeeAvatar(
                photoUrl = employee.photourl,
                nome = employee.nome,
                cognome = employee.cognome,
                size = 80.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nome e cognome
            Text(
                text = "${employee.nome} ${employee.cognome}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            // Posizione
            Text(
                text = employee.posizioneLavorativa,
                fontSize = 16.sp,
                color = Color(0xFF7F8C8D),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Dipartimento
            Text(
                text = employee.dipartimento,
                fontSize = 14.sp,
                color = Color(0xFF95A5A6),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Informazioni di contatto
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoRow("Email", employee.email)
//                InfoRow("Telefono", employee.phone)
//                InfoRow("Data Assunzione", employee.hireDate)
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Testo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C3E50),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color(0xFF7F8C8D),
            modifier = Modifier.weight(1f)
        )
    }
}