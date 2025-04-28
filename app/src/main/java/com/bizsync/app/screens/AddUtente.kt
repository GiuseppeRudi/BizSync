package com.bizsync.app.screens

import ImageUi
import android.util.Log
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.AddUtenteViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun AddUtente(
    onChooseAzienda : () -> Unit
) {


    val addutenteviewmodel :  AddUtenteViewModel = hiltViewModel()
    val userviewmodel = LocalUserViewModel.current
    val uiScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (addutenteviewmodel.currentStep.value) {
            1 -> StepOne(addutenteviewmodel)
            2 -> StepTwo(addutenteviewmodel)
            3 -> StepThree(addutenteviewmodel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (addutenteviewmodel.currentStep.value > 1) {
                Button(onClick = { addutenteviewmodel.currentStep.value-- })
                {
                    Text("Indietro")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (addutenteviewmodel.currentStep.value < 3) {
                Button(onClick = { addutenteviewmodel.currentStep.value++ }) {
                    Text("Avanti")
                }
            } else {
                Button(onClick = {
                    uiScope.launch {
                        // 1) chiamo la suspend addUser e aspetto che finisca
                        addutenteviewmodel.addUser()

                        // 2) poi propaghiamo lo User e l'UID ai tuoi viewmodel
                        userviewmodel.user.value = addutenteviewmodel.utente.value
                        userviewmodel.uid.value  = addutenteviewmodel.utente.value?.uid.orEmpty()

                        Log.d("AZIENDA_DEBUG", "USER VM user: ${userviewmodel.user.value}")
                        Log.d("AZIENDA_DEBUG", "USER VM uid: ${userviewmodel.uid.value}")

                        // 3) infine navighiamo
                        onChooseAzienda()
                    }
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

    // DA VERIFCIARE
    addutenteviewmodel.email.value = currentuser?.email.toString()
    addutenteviewmodel.uid.value = currentuser?.uid.toString()
    addutenteviewmodel.photourl.value = currentuser?.photoUrl.toString()


    //val uri = addutenteviewmodel.photourl?.value.let { Uri.parse(it) }
    // ImageUi(uri)

    // BOTTONE MODIFICA CHE PERMETTE DI SCEGLEIRE UN' ALTRA IMMAGINE PROFILO DALLA GALLERIA E SALVARLA IN LOCALE SU UNA CARTELLA DELL'APP


    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Nome ")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = addutenteviewmodel.nome.value,
        onValueChange = { addutenteviewmodel.nome.value = it },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.padding(16.dp))

    Text("Schermata 1: Inserisci Cognome")

    Spacer(modifier = Modifier.padding(16.dp))

    OutlinedTextField(
        value = addutenteviewmodel.cognome.value,
        onValueChange = { addutenteviewmodel.cognome.value = it },
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

