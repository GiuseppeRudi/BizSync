package com.bizsync.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bizsync.ui.components.Calendar
import androidx.hilt.navigation.compose.hiltViewModel



import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color

import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.ui.components.DialogAddShif
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.viewmodels.CalendarViewModel
import com.bizsync.ui.viewmodels.DialogAddShiftViewModel


@Composable
fun PianificaScreen() {


    val dialogviewmodel : DialogAddShiftViewModel = hiltViewModel()

    val calendarviewmodel : CalendarViewModel = hiltViewModel()

    Log.d("TURNI_DEBUG", "SONO ENTRATO")
    Log.d("VERIFICA_GIORNO", "Sono un cavallo " + calendarviewmodel.selectionData.value.toString())




    val giornoSelezionato = calendarviewmodel.selectionData.value
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
                items(dialogviewmodel.itemsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = item.Nome,
                            modifier = Modifier.padding(16.dp),
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spazio tra lista e bottone


            RoundedButton(
                true,
                onShow = { calendarviewmodel.showDialogShift.value = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Posiziona il bottone in basso a destra
                    .padding(16.dp) // Aggiunge margine dai bordi
            )
        }

    }
    DialogAddShif(showDialog= calendarviewmodel.showDialogShift.value, onDismiss = { calendarviewmodel.showDialogShift.value = false })

}



@Preview
@Composable
fun PianificaScreenPreview() {
    PianificaScreen()
}
