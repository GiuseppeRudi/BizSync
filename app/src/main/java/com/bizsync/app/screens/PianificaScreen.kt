package com.bizsync.app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizsync.ui.components.Calendar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.bizsync.app.OnboardingFlow
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.components.TurnoDialog
import com.bizsync.ui.viewmodels.PianificaViewModel


@Composable
fun PianificaScreen() {




    val pianificaVM : PianificaViewModel = hiltViewModel()

    val userviewmodel = LocalUserViewModel.current


    val azienda by userviewmodel.azienda.collectAsState()


    val onBoardingDone by pianificaVM.onBoardingDone.collectAsState()


    LaunchedEffect(Unit) {
        pianificaVM.checkOnBoardingStatus(azienda)
    }


    when (onBoardingDone) {
        null  -> CircularProgressIndicator()
        false -> SetupPianificaScreen(onSetupComplete = { pianificaVM.setOnBoardingDone(true) })
        true  ->  PianificaCore(pianificaVM)

    }

}



@Composable
fun PianificaCore(
    pianificaVM : PianificaViewModel,
)
{

    val selectionData by pianificaVM.selectionData.collectAsState()
    val itemsList by pianificaVM.itemsList.collectAsState()
    val showDialogShift by pianificaVM.showDialogShift.collectAsState()


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


            RoundedButton(
                selectionData,
                onShow = { pianificaVM.onShowDialogShiftChanged(true)},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

    }
    TurnoDialog(showDialog = showDialogShift, selectionData, onDismiss = { pianificaVM.onShowDialogShiftChanged(false)} , pianificaVM)

}

