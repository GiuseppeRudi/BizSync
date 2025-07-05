package com.bizsync.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.viewmodels.CompanyViewModel
import com.bizsync.ui.viewmodels.PianificaViewModel
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizsync.domain.constants.enumClass.CompanyOperation

import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.TurnoFrequente



@Composable
fun CompanyManagementScreen(onBackClick: () -> Unit) {


    val companyVm : CompanyViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current
    val userState by userviewmodel.uiState.collectAsState()

    val azienda = userState.azienda


    val companyState by companyVm.uiState.collectAsState()

    val onBoardingDone = companyState.onBoardingDone

    LaunchedEffect(Unit) {
        companyVm.checkOnBoardingStatus(azienda)
    }


    when (onBoardingDone) {
        null  -> CircularProgressIndicator()
        false -> SetupPianificaScreen(onSetupComplete = { companyVm.setOnBoardingDone(true) })
        true  ->  CompanyCore(companyVm, azienda)

    }
}

@Composable
fun CompanyCore(companyVm: CompanyViewModel, azienda: AziendaUi) {
    val userViewModel = LocalUserViewModel.current
    val userState by userViewModel.uiState.collectAsState()
    val companyState by companyVm.uiState.collectAsState()

    val azienda = userState.azienda
    val selectedOperation = companyState.selectedOperation

    // Schermata di gestione specifica
    when (selectedOperation) {
        CompanyOperation.DIPARTIMENTI -> {
            DipartimentiManagementScreen(companyVm, azienda.areeLavoro, azienda.idAzienda)
        }

        CompanyOperation.GESTIONE_INVITI  -> {
            InviteManagementScreen(companyVm)
        }

        CompanyOperation.TURNI_FREQUENTI -> {
            TurniFrequentiManagementScreen(
                currentTurni = azienda.turniFrequenti,
                onBackClick = { companyVm.setSelectedOperation(null) },
                onSaveChanges = { nuoviTurni ->
                    companyVm.setSelectedOperation(null)
                },
                isLoading = companyState.isLoading
            )
        }

        null -> OperationSelectorScreen(
            onOperationSelected = { operation ->
                companyVm.setSelectedOperation(operation)
            }
        )
    }
}

