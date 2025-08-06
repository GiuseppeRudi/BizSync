package com.bizsync.ui.screens


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.domain.constants.enumClass.InviteView
import com.bizsync.ui.viewmodels.CompanyViewModel
import com.bizsync.ui.viewmodels.ManageInviteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteManagementScreen(
    companyVM: CompanyViewModel
) {
    val scaffoldVm = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current
    val inviteVM: ManageInviteViewModel = hiltViewModel()

    val inviteState by inviteVM.uiState.collectAsState()
    val userState by userVM.uiState.collectAsState()
    val azienda = userState.azienda

    // Inizializza sempre con la vista cronologia
    LaunchedEffect(Unit) {
        scaffoldVm.onFullScreenChanged(false)
        if (inviteState.currentView != InviteView.CREATE_INVITE) {
            inviteVM.setCurrentView(InviteView.VIEW_INVITES)
        }
    }

    when (inviteState.currentView) {
        InviteView.VIEW_INVITES -> {
            ViewInvitesScreen(
                inviteVM = inviteVM,
                idAzienda = azienda.idAzienda,
                onBackClick = { companyVM.setSelectedOperation(null) },
                onCreateInvite = { inviteVM.setCurrentView(InviteView.CREATE_INVITE) }
            )
        }

        InviteView.CREATE_INVITE -> {
            CreateInviteScreen(
                inviteVM = inviteVM,
                azienda = azienda,
                onBack = {
                    inviteVM.setCurrentView(InviteView.VIEW_INVITES)
                    inviteVM.loadInvites(azienda.idAzienda)
                }
            )
        }
    }
}

