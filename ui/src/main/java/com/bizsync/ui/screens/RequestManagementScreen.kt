package com.bizsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.RequestViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestManagementScreen(
    onBackClick: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Richieste in Attesa", "Cronologia")

    val userVM = LocalUserViewModel.current
    val scaffoldVM = LocalScaffoldViewModel.current
    val requestVM : RequestViewModel = hiltViewModel()

    val userState by userVM.uiState.collectAsState()
    val approver = userState.user.cognome + " " + userState.user.nome

    val idAzienda = userState.azienda.idAzienda

    LaunchedEffect(Unit) {
        scaffoldVM.onFullScreenChanged(true)
        requestVM.fetchAllRequests(idAzienda)
        requestVM.fetchAllContract(idAzienda)
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Gestione Richieste",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> PendingRequestsContent(requestVM, approver)
            1 -> HistoryContent(requestVM)
        }
    }
}


@Composable
fun EmptyStateContent(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


