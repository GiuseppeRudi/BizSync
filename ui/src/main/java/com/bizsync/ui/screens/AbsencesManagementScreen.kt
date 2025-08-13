package com.bizsync.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.model.Contratto
import com.bizsync.ui.components.AbsenceRequestCard
import com.bizsync.ui.components.EnhancedStatisticProgressRow
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.AbsenceViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbsencesManagementScreen(onBackClick: () -> Unit) {
    val absenceVM: AbsenceViewModel = hiltViewModel()
    val absenceState by absenceVM.uiState.collectAsState()
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()


    LaunchedEffect(Unit) {
        absenceVM.fetchAllAbsences(userState.user.uid)
    }

    if (!absenceState.hasLoadedAbsences) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    else{
        Box(modifier = Modifier.fillMaxSize()) {
            when (absenceState.selectedTab) {
                0 -> RequestsListContent(absenceVM, onBackClick)
                2 -> NewAbsenceRequestScreen(
                    userVM,
                    absenceVM,
                    onDismiss = { absenceVM.setSelectedTab(0) },
                    onSubmit = { absenceVM.setSelectedTab(0) }
                )
            }
        }

    }


}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestsListContent(absenceVM: AbsenceViewModel, onBack: () -> Unit) {
    val uiState by absenceVM.uiState.collectAsState()
    val requests = uiState.absences

    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val contratto = userState.contratto

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Le Mie Assenze",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { absenceVM.setSelectedTab(2) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuova richiesta",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->


        if (requests.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp)) // più spazio

                contratto.let {
                    StatisticsCard(contratto = it, absenceVM)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Nessuna richiesta di assenza",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    bottom = 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatisticsCard(contratto = contratto, absenceVM)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Le Mie Richieste",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .weight(1f)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }

                items(requests) { request ->
                    AbsenceRequestCard(request = request)
                }
            }
        }
    }
}



@Composable
private fun StatisticsCard(contratto: Contratto, absenceVM: AbsenceViewModel) {

    val uiState by absenceVM.uiState.collectAsState()

   val pendingStats = uiState.pendingStats

    LaunchedEffect(uiState.absences) {
        absenceVM.changePendingStatus()    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Riepilogo Anno Corrente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ferie
            EnhancedStatisticProgressRow(
                label = "Ferie",
                approved = contratto.ferieUsate,
                pending = pendingStats.pendingVacationDays,
                max = contratto.ccnlInfo.ferieAnnue,
                unit = "giorni",
                color = Color(0xFF4CAF50),
                icon = Icons.Default.BeachAccess
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ROL
            EnhancedStatisticProgressRow(
                label = "Permessi ROL",
                approved = contratto.rolUsate,
                pending = pendingStats.pendingRolHours,
                max = contratto.ccnlInfo.rolAnnui,
                unit = "ore",
                color = Color(0xFF2196F3),
                icon = Icons.Default.Schedule
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Malattia
            EnhancedStatisticProgressRow(
                label = "Malattia",
                approved = contratto.malattiaUsata,
                pending = pendingStats.pendingSickDays,
                max = contratto.ccnlInfo.malattiaRetribuita,
                unit = "giorni",
                color = Color(0xFFFF9800),
                icon = Icons.Default.LocalHospital
            )
        }
    }
}

