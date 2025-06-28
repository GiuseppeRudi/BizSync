package com.bizsync.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalScaffoldViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.viewmodels.PianificaViewModel


@Composable
fun PianificaScreen() {


    val pianificaVM : PianificaViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current
    val userState by userviewmodel.uiState.collectAsState()

    val manager = userState.user.isManager
    val azienda = userState.azienda


    val pianificaState by pianificaVM.uistate.collectAsState()
    val onBoardingDone = pianificaState.onBoardingDone

    LaunchedEffect(Unit) {
        if(manager)
        {
            pianificaVM.checkOnBoardingStatus(azienda)
        }
        else
        {
            pianificaVM.setOnBoardingDone(true)
        }
    }


    when (onBoardingDone) {
        null  -> CircularProgressIndicator()
        false -> SetupPianificaScreen(onSetupComplete = { pianificaVM.setOnBoardingDone(true) })
        true  ->  PianificaCore(pianificaVM, manager)

    }

}



@Composable
fun PianificaCore(
    pianificaVM : PianificaViewModel,
    manager : Boolean
)
{

    val scaffoldVM = LocalScaffoldViewModel.current
    val userVM = LocalUserViewModel.current


    val pianificaState by pianificaVM.uistate.collectAsState()
    val selectionData = pianificaState.selectionData
    val itemsList = pianificaState.itemsList
    val showDialogShift = pianificaState.showDialogShift


    LaunchedEffect(selectionData) {
        selectionData?.let { giorno ->
            pianificaVM.caricaturni(giorno)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Calendar(pianificaVM)

        Spacer(modifier = Modifier.height(8.dp))

        Box(){

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                items(itemsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = item.nome,
                            modifier = Modifier.padding(16.dp),
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            if (manager) {
                RoundedButton(
                    selectionData,
                    onShow = { pianificaVM.onShowDialogShiftChanged(true) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }

        }

    }

    if(showDialogShift)
    {
        TurnoScreen(selectionData, onBack = { pianificaVM.onShowDialogShiftChanged(false); scaffoldVM.onFullScreenChanged(true)} , pianificaVM, userVM, scaffoldVM)
    }

}


