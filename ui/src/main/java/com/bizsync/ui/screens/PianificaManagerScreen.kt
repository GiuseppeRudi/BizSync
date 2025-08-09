package com.bizsync.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.domain.constants.enumClass.PianificaScreenManager
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.components.HeaderTurniManagerWithLoading
import com.bizsync.ui.components.SelectionDataEmptyCard
import com.bizsync.ui.navigation.LocalScaffoldViewModel
import com.bizsync.ui.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.PianificaManagerViewModel
import com.bizsync.ui.viewmodels.PianificaViewModel


@Composable
fun PianificaManagerScreen(
    pianificaVM: PianificaViewModel,
) {
    val userVM = LocalUserViewModel.current


    val scaffoldVM = LocalScaffoldViewModel.current
    LaunchedEffect(Unit) {scaffoldVM.onFullScreenChanged(false) }

    val userState by userVM.uiState.collectAsState()

    val pianificaState by pianificaVM.uistate.collectAsState()
    val selectionData = pianificaState.selectionData

    val weeklyisIdentical = pianificaState.weeklyisIdentical
    val weeklyShiftRiferimento = pianificaState.weeklyShiftRiferimento
    val weeklyShiftAttuale = pianificaState.weeklyShiftAttuale
    val managerVM: PianificaManagerViewModel = hiltViewModel()

    var dipartimenti by remember { mutableStateOf(emptyList<AreaLavoro>()) }
    val isIdentical = pianificaState.weeklyisIdentical

    Log.d("PianificaManagerCore", "weeklyisIdentical: $weeklyisIdentical")
    LaunchedEffect(weeklyShiftRiferimento, weeklyShiftAttuale) {
        val riferimento = weeklyShiftRiferimento
        val attuale = weeklyShiftAttuale

        pianificaVM.setWeeklyShiftIdentical(riferimento != null && attuale != null && riferimento.id == attuale.id)

        val isIdentical = pianificaState.weeklyisIdentical

        if(riferimento!=null && (attuale==null || (attuale!=null && isIdentical))) {
            dipartimenti = riferimento.dipartimentiAttivi
            pianificaVM.syncTurniAvvio(riferimento.weekStart)
            managerVM.setTurniSettimanali(riferimento.weekStart, userState.azienda.idAzienda)
            managerVM.inizializzaDatiWeeklyRiferimento(riferimento.dipendentiAttivi)

        }

        if (attuale != null) {
            dipartimenti = attuale.dipartimentiAttivi
            managerVM.inizializzaDatiDipendenti(attuale.dipendentiAttivi)
            managerVM.setTurniSettimanali(attuale.weekStart, userState.azienda.idAzienda)
        }
    }



    LaunchedEffect(selectionData)
    {
        pianificaVM.backToMain()

        if(selectionData!=null )
        { pianificaVM.getWeeklyShiftCorrente(selectionData) }
    }

    val loading = pianificaState.loadingWeekly
    val currentScreen by pianificaVM.currentScreen.collectAsState()

    val hasUnsavedChanges = pianificaState.hasUnsavedChanges
    val isSyncing = pianificaState.isSyncing

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if(currentScreen != PianificaScreenManager.CREATE_SHIFT)
        {
            PlanningHeader(
                weeklyShift = weeklyShiftRiferimento,
                hasUnsavedChanges = hasUnsavedChanges,
                isLoading = isSyncing,
                onSync = {
                    pianificaVM.syncTurni(weeklyShiftAttuale?.weekStart)
                },
                setExpanded = isIdentical,
                onStatoSettimana = { nuovoStato ->

                    pianificaVM.changeStatoWeeklyAttuale(nuovoStato)
                }
            )

            HeaderTurniManagerWithLoading(
                loading = loading,
                weeklyShiftAttuale = weeklyShiftAttuale,
                weeklyShiftRiferimento = weeklyShiftRiferimento,
                selectionData = selectionData
            )



            // Calendario
            Calendar(pianificaVM)

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Contenuto principale
        selectionData?.let { giornoSelezionato ->
            when (currentScreen) {

                PianificaScreenManager.MAIN -> {

                    PianificaGiornata(
                        dipartimenti = dipartimenti,
                        giornoSelezionato = selectionData,
                        managerVM = managerVM,
                        onDipartimentoClick = { pianificaVM.openGestioneTurni(it) },
                    )
                }

                PianificaScreenManager.GESTIONE_TURNI_DIPARTIMENTO -> {
                    pianificaState.dipartimento?.let { dip ->
                        GestioneTurniDipartimentoScreen(
                            dipartimento = dip,
                            giornoSelezionato = selectionData,
                            weeklyIsIdentical = weeklyisIdentical,
                            onCreateShift = { pianificaVM.openCreateShift() },
                            managerVM = managerVM,
                            weeklyShift = weeklyShiftAttuale,
                            onHasUnsavedChanges = { pianificaVM.setHasUnsavedChanges(it) },
                            onBack = { pianificaVM.backToMain() })
                    }
                }

                PianificaScreenManager.CREATE_SHIFT -> {
                    pianificaState.dipartimento?.let { dip ->
                        TurnoScreen(
                            dipartimento = dip,
                            giornoSelezionato = giornoSelezionato,
                            onHasUnsavedChanges = { pianificaVM.setHasUnsavedChanges(it) },
                            onBack = { pianificaVM.setDipartimentoScreen(dip) },
                            managerVM = managerVM,
                        )
                    }
                }


            }
        } ?: run { SelectionDataEmptyCard() }
    }
}
