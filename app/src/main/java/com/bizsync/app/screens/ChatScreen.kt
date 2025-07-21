package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.bizsync.ui.viewmodels.ChatScreenViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.domain.model.User

@Composable
fun ChatScreen() {


    // Dipendente di esempio 1
    val dipendente1 = User(
        uid = "user1",
        email = "lucia.reception@example.com",
        nome = "Lucia",
        cognome = "Bianchi",
        photourl = "",
        idAzienda = "ZNTzPHOA2xyJMgymaFN7",
        isManager = false,
        posizioneLavorativa = "Receptionist",
        dipartimento = "c678978a-07d4-43cd-b909-08fd881e62b8" // Reception
    )

// Dipendente di esempio 2
    val dipendente2 = User(
        uid = "user2",
        email = "marco.conta@example.com",
        nome = "Marco",
        cognome = "Verdi",
        photourl = "",
        idAzienda = "ZNTzPHOA2xyJMgymaFN7",
        isManager = false,
        posizioneLavorativa = "Contabile",
        dipartimento = "ddf2acb0-d62e-45f4-9d37-9f43a0f15b13" // Contabilità
    )

// Dipendente di esempio 3
    val dipendente3 = User(
        uid = "user3",
        email = "elisa.hr@example.com",
        nome = "Elisa",
        cognome = "Neri",
        photourl = "",
        idAzienda = "ZNTzPHOA2xyJMgymaFN7",
        isManager = false,
        posizioneLavorativa = "Risorse Umane",
        dipartimento = "cf1edb95-6954-4f55-9aed-449cee490cae" // Risorse Umane
    )

// Manager
    val managerUser = User(
        uid = "manager1",
        email = "franco.manager@example.com",
        nome = "Franchino",
        cognome = "Criminale",
        photourl = "",
        idAzienda = "ZNTzPHOA2xyJMgymaFN7",
        isManager = true,
        posizioneLavorativa = "Direttore",
        dipartimento = "a8cd75a7-dda5-4bf2-857b-0ba960994640" // Supervisione
    )

// Lista completa da passare al Composable
    val listaDipendenti = listOf(dipendente1, dipendente2, dipendente3)

    //  AIChatScreen(onBackClick = {})

    EmployeeChatScreen(
        currentUser = managerUser,
        employees = listaDipendenti,
        onBackClick = { /* navigazione indietro */ }
    )



}
