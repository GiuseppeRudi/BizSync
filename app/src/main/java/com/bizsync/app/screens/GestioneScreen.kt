package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.viewmodels.GestioneViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.UserViewModel

// Enum per gestire le schermate
enum class ManagementScreen {
    MAIN, EMPLOYEES
}


data class ManagementCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val onClick: () -> Unit
)





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestioneScreen() {
    val gestioneVM: GestioneViewModel = hiltViewModel()
    val showInviteDialog by gestioneVM.showDialog.collectAsState()
    val userVM = LocalUserViewModel.current

    val checkManager by userVM.user.collectAsState()


    LaunchedEffect(checkManager.manager) {
        //gestioneVM.fetchManagementCards(checkManager.manager)
    }

    // Stato per gestire la navigazione interna
    var currentScreen by remember { mutableStateOf(ManagementScreen.MAIN) }

    // Naviga tra le schermate con animazioni
    AnimatedVisibility(
        visible = currentScreen == ManagementScreen.MAIN,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
    ) {
        MainManagementScreen(
            gestioneVM = gestioneVM,
            showInviteDialog = showInviteDialog,
            userVM = userVM,
            onNavigateToEmployees = { currentScreen = ManagementScreen.EMPLOYEES }
        )
    }

    AnimatedVisibility(
        visible = currentScreen == ManagementScreen.EMPLOYEES,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        EmployeeManagementScreen(
            onBackClick = { currentScreen = ManagementScreen.MAIN }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainManagementScreen(
    gestioneVM: GestioneViewModel,
    showInviteDialog: Boolean,
    userVM: UserViewModel, // Sostituisci con il tipo corretto
    onNavigateToEmployees: () -> Unit
) {

  //  val managementCards by gestioneVM.managementCards.collectAsState()

    // Definizione delle card di gestione
    val managementCards = remember {
        listOf(
            ManagementCard(
                title = "Dipendenti",
                description = "Gestisci il personale e le risorse umane",
                icon = Icons.Default.People,
                gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
            ) { onNavigateToEmployees() },

            ManagementCard(
                title = "Progetti",
                description = "Monitora e gestisci tutti i progetti attivi",
                icon = Icons.Default.Assignment,
                gradient = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
            ) { /* Navigate to Projects */ },

            ManagementCard(
                title = "Finanze",
                description = "Controlla budget, fatture e pagamenti",
                icon = Icons.Default.AccountBalance,
                gradient = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
            ) { /* Navigate to Finance */ },

            ManagementCard(
                title = "Inventario",
                description = "Gestisci magazzino e forniture",
                icon = Icons.Default.Inventory,
                gradient = listOf(Color(0xFF43e97b), Color(0xFF38f9d7))
            ) { /* Navigate to Inventory */ },

            ManagementCard(
                title = "Clienti",
                description = "CRM e gestione relazioni clienti",
                icon = Icons.Default.ContactPhone,
                gradient = listOf(Color(0xFFfa709a), Color(0xFFfee140))
            ) { /* Navigate to Customers */ },

            ManagementCard(
                title = "Reportistica",
                description = "Analytics e report aziendali",
                icon = Icons.Default.Analytics,
                gradient = listOf(Color(0xFFa8edea), Color(0xFFfed6e3))
            ) { /* Navigate to Reports */ },

            ManagementCard(
                title = "Impostazioni",
                description = "Configurazioni e preferenze sistema",
                icon = Icons.Default.Settings,
                gradient = listOf(Color(0xFFffecd2), Color(0xFFfcb69f))
            ) { /* Navigate to Settings */ },

            ManagementCard(
                title = "Sicurezza",
                description = "Gestisci permessi e sicurezza dati",
                icon = Icons.Default.Security,
                gradient = listOf(Color(0xFF8360c3), Color(0xFF2ebf91))
            ) { /* Navigate to Security */ }
        )
    }

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
            .padding(16.dp)
    ) {

        // Grid di card sfalsate
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(managementCards.chunked(2)) { rowIndex, cardPair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cardPair.forEachIndexed { cardIndex, card ->
                        val isLeft = cardIndex == 0
                        val offsetY = if (isLeft) 0.dp else 12.dp

                        ManagementCardItem(
                            card = card,
                            modifier = Modifier
                                .weight(1f)
                                .offset(y = offsetY)
                        )
                    }

                    // Se c'è solo una card nella riga, aggiungi spazio vuoto
                    if (cardPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Spazio extra per il FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

    }
}

@Composable
fun ManagementCardItem(
    card: ManagementCard,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .scale(scale)
            .height(140.dp)
            .clickable {
                isPressed = true
                card.onClick()
            }
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = card.gradient,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = card.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = card.description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }

}
