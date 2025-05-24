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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bizsync.ui.components.Calendar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.components.TurnoDialog
import com.bizsync.ui.viewmodels.CalendarViewModel
import com.bizsync.ui.viewmodels.TurnoDialogViewModel



@Composable
fun PianificaScreen() {

    val navController = LocalNavController.current
    val userviewmodel = LocalUserViewModel.current

    val user by userviewmodel.user.collectAsState()
    val uid by userviewmodel.uid.collectAsState()

    Log.d("LOGINREPO_DEBUG", "FUNZIONA? " +uid)

    val dialogviewmodel : TurnoDialogViewModel = hiltViewModel()
    val calendarviewmodel : CalendarViewModel = hiltViewModel()


    val showDialogShift by calendarviewmodel.showDialogShift.collectAsState()
    val selectionData by calendarviewmodel.selectionData.collectAsState()

    val itemsList by dialogviewmodel.itemsList.collectAsState()

    Log.d("TURNI_DEBUG", "SONO ENTRATO")
    Log.d("VERIFICA_GIORNO", "GIORNO" + selectionData.toString())




    val giornoSelezionato = selectionData
    if (giornoSelezionato!=null)
    {
        dialogviewmodel.caricaturni(giornoSelezionato)
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Calendar(calendarviewmodel)

        Spacer(modifier = Modifier.height(8.dp)) // Spazio tra calendario e lista

        Box(){

            // La lista occupa tutto lo spazio disponibile al centro
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

            Spacer(modifier = Modifier.height(8.dp)) // Spazio tra lista e bottone


            RoundedButton(
                giornoSelezionato,
                onShow = { calendarviewmodel.onShowDialogShiftChanged(true)},
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Posiziona il bottone in basso a destra
                    .padding(16.dp) // Aggiunge margine dai bordi
            )
        }

    }
    TurnoDialog(showDialog = showDialogShift, giornoSelezionato, onDismiss = { calendarviewmodel.onShowDialogShiftChanged(false)})

}


@Preview
@Composable
fun PianificaScreenPreview() {
    PianificaScreen()
}
