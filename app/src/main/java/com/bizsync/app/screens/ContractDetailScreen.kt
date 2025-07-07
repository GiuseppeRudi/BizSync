package com.bizsync.app.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.EmployeeSection
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.model.UserUi
import com.bizsync.ui.viewmodels.EmployeeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    employee: UserUi,
    employeeVm : EmployeeViewModel,
) {


    val uiState by employeeVm.uiState.collectAsState()

    val contract = uiState.contract
    val isLoading = uiState.isLoading

//    LaunchedEffect(employee.id) {
//        userVm.loadEmployeeContract(employee.id)
//    }

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
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Contratto - ${employee.nome} ${employee.cognome}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            },
            navigationIcon = {
                IconButton(onClick = {employeeVm.setCurrentSection(EmployeeSection.MAIN)}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color(0xFF2C3E50)
                    )
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF3498DB)
                )
            }
        } else if (contract != null) {
            ContractContent(contract!!)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nessun contratto trovato",
                    fontSize = 16.sp,
                    color = Color(0xFF7F8C8D)
                )
            }
        }
    }
}


@Composable
fun ContractContent(contract: Contratto) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Informazioni generali contratto
        item {
            SectionCard(
                title = "Informazioni Generali",
                icon = Icons.Default.Assignment,
                iconColor = Color(0xFF3498DB)
            ) {
                InfoRow("Posizione", contract.posizioneLavorativa)
                InfoRow("Dipartimento", contract.dipartimento)
                InfoRow("Tipo Contratto", contract.tipoContratto)
                InfoRow("Ore Settimanali", contract.oreSettimanali)
                InfoRow("Settore Aziendale", contract.settoreAziendale)
                InfoRow("Data Inizio", contract.dataInizio)
            }
        }

        // Informazioni CCNL
        item {
            SectionCard(
                title = "Informazioni CCNL",
                icon = Icons.Default.Business,
                iconColor = Color(0xFF2ECC71)
            ) {
                InfoRow("Settore", contract.ccnlInfo.settore)
                InfoRow("Ruolo", contract.ccnlInfo.ruolo)
                InfoRow("Ferie Annue", "${contract.ccnlInfo.ferieAnnue} giorni")
                InfoRow("ROL Annui", "${contract.ccnlInfo.rolAnnui} giorni")
                InfoRow("Stipendio Annuale Lordo", "€${contract.ccnlInfo.stipendioAnnualeLordo}")
                InfoRow("Malattia Retribuita", "${contract.ccnlInfo.malattiaRetribuita} giorni")
            }
        }

        // Dettagli economici
        item {
            SectionCard(
                title = "Dettagli Economici",
                icon = Icons.Default.Euro,
                iconColor = Color(0xFFF39C12)
            ) {
                val stipendioMensile = contract.ccnlInfo.stipendioAnnualeLordo / 12
                InfoRow("Stipendio Mensile Lordo", "€${String.format("%.2f", stipendioMensile)}")
                InfoRow("Stipendio Annuale Lordo", "€${contract.ccnlInfo.stipendioAnnualeLordo}")
            }
        }

        // Permessi e congedi
        item {
            SectionCard(
                title = "Permessi e Congedi",
                icon = Icons.Default.CalendarMonth,
                iconColor = Color(0xFF9B59B6)
            ) {
                InfoRow("Ferie Annue", "${contract.ccnlInfo.ferieAnnue} giorni")
                InfoRow("ROL Annui", "${contract.ccnlInfo.rolAnnui} giorni")
                InfoRow("Malattia Retribuita", "${contract.ccnlInfo.malattiaRetribuita} giorni")
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header della sezione
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
            }

            // Contenuto della sezione
            content()
        }
    }
}