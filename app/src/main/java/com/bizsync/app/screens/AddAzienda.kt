package com.bizsync.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.components.DipendentiSelector
import com.bizsync.ui.components.SettoreSelector
import com.bizsync.ui.viewmodels.AddAziendaViewModel


@Composable
fun AddAzienda(onTerminate : () -> Unit) {

    val addaziendaviewmodel :  AddAziendaViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current

    val uid = userviewmodel.uid.value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (addaziendaviewmodel.currentStep.value) {
            1 -> StepOne(addaziendaviewmodel)
            2 -> StepTwo(addaziendaviewmodel)
            3 -> StepThree(addaziendaviewmodel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (addaziendaviewmodel.currentStep.value > 1) {
                Button(onClick = { addaziendaviewmodel.currentStep.value-- }) {
                    Text("Indietro")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (addaziendaviewmodel.currentStep.value < 3) {
                Button(onClick = { addaziendaviewmodel.currentStep.value++ }) {
                    Text("Avanti")
                }
            } else {
                Button(onClick = { addaziendaviewmodel.aggiungiAzienda(uid , userviewmodel)
                                    onTerminate()
                                    })
                {
                    Text("Conferma")
                }
            }
        }
    }
}


@Composable
fun StepOne(addaziendaviewmodel : AddAziendaViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Nome Azienda")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = addaziendaviewmodel.nomeAzienda.value,
        onValueChange = { addaziendaviewmodel.nomeAzienda.value = it },
        modifier = Modifier.fillMaxWidth()
    )
}



@Composable
fun StepTwo(addaziendaviewmodel: AddAziendaViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 2: Inserisci Numero Indipendenti")

    DipendentiSelector(addaziendaviewmodel)

}

@Composable
fun StepThree(addaziendaviewmodel: AddAziendaViewModel) {
    Text("Schermata 3:  Altri dati utili " )

    SettoreSelector(addaziendaviewmodel)
}



