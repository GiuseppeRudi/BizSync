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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.AddUtenteViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue


@Composable
fun AddUtente(
    onChooseAzienda : () -> Unit
) {


    val addutenteviewmodel :  AddUtenteViewModel = hiltViewModel()
    val currentStep by addutenteviewmodel.currentStep.collectAsState()
    val errore by addutenteviewmodel.erroreSalvataggio.collectAsState()

    val userviewmodel = LocalUserViewModel.current

    val isUserAdded by addutenteviewmodel.isUserAdded.collectAsState()

    LaunchedEffect(isUserAdded) {
        if (isUserAdded) {
            onChooseAzienda()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (currentStep) {
            1 -> StepOne(addutenteviewmodel)
            2 -> StepTwo(addutenteviewmodel)
            3 -> StepThree(addutenteviewmodel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (currentStep > 1) {
                Button(onClick = { addutenteviewmodel.onCurrentStepDown()})
                {
                    Text("Indietro")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (currentStep < 3) {
                Button(onClick = { addutenteviewmodel.onCurrentStepUp() }) {
                    Text("Avanti")
                }
            } else {
                Button(onClick = {
                        addutenteviewmodel.addUserAndPropaga(userviewmodel)
                }) {
                    Text("Conferma")
                }
            }
        }
    }


}

@Composable
fun StepOne(addutenteviewmodel : AddUtenteViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    val currentuser =  FirebaseAuth.getInstance().currentUser

    addutenteviewmodel.onEmailChanged(currentuser?.email.toString())
    addutenteviewmodel.onUidChanged(currentuser?.uid.toString())
    addutenteviewmodel.onPhotoUrlChanged(currentuser?.photoUrl.toString())

    val nome by addutenteviewmodel.nome.collectAsState()
    val cognome by addutenteviewmodel.cognome.collectAsState()

    //val uri = addutenteviewmodel.photourl?.value.let { Uri.parse(it) }
    // ImageUi(uri)

    // BOTTONE MODIFICA CHE PERMETTE DI SCEGLEIRE UN' ALTRA IMMAGINE PROFILO DALLA GALLERIA E SALVARLA IN LOCALE SU UNA CARTELLA DELL'APP


    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Nome ")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = nome,
        onValueChange = { addutenteviewmodel.onNomeChanged(it) },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Cognome")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = cognome,
        onValueChange = { addutenteviewmodel.onCognomeChanged(it) },
        modifier = Modifier.fillMaxWidth()
    )
}



@Composable
fun StepTwo(addutenteviewmodel: AddUtenteViewModel) {

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 2: PUO ESSERE AGGIUNTE ALTRE FUNZIONALITA LLA'UTENTE ")
}


@Composable
fun StepThree(addutenteviewmodel: AddUtenteViewModel) {
    Text("Schermata 3:  ALTRE COSE  " )

}




