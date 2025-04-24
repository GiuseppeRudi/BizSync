package com.bizsync.app.screens

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.ui.components.DipendentiSelector
import com.bizsync.ui.components.SettoreSelector
import com.bizsync.ui.viewmodels.WelcomeViewModel


@Composable
fun WelcomeScreen() {


    val welcomeviewmodel :  WelcomeViewModel = hiltViewModel()


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (welcomeviewmodel.currentStep.value) {
            1 -> StepOne(welcomeviewmodel)
            2 -> StepTwo(welcomeviewmodel)
            3 -> StepThree(welcomeviewmodel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (welcomeviewmodel.currentStep.value > 1) {
                Button(onClick = { welcomeviewmodel.currentStep.value-- }) {
                    Text("Indietro")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (welcomeviewmodel.currentStep.value < 3) {
                Button(onClick = { welcomeviewmodel.currentStep.value++ }) {
                    Text("Avanti")
                }
            } else {
                Button(onClick = { welcomeviewmodel.aggiungiAzienda() }) {
                    Text("Conferma")
                }
            }
        }
    }
}


@Composable
fun StepOne(welcomeviewmodel : WelcomeViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Nome Azienda")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = welcomeviewmodel.nomeAzienda.value,
        onValueChange = { welcomeviewmodel.nomeAzienda.value = it },
        modifier = Modifier.fillMaxWidth()
    )
}



@Composable
fun StepTwo(welcomeviewmodel: WelcomeViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 2: Inserisci Numero Indipendenti")

    DipendentiSelector(welcomeviewmodel)

}

@Composable
fun StepThree(welcomeviewmodel: WelcomeViewModel) {
    Text("Schermata 3:  Altri dati utili " )

    SettoreSelector(welcomeviewmodel)
}



