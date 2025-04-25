package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.AddUtente
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import android.util.Log


@Composable
fun MainApp() {

    val splashScreenViewModel : SplashViewModel = viewModel()
    var currentUser = FirebaseAuth.getInstance().currentUser
    var userViewModel = LocalUserViewModel.current

    // UNA VOLTA EFFETTUATO IL LOGIN CON GOOGLE MI PRENDO UID
    if(currentUser !=null)
    {

        var uid = currentUser.uid
        userViewModel.getUser(uid)

        Log.d("LOGIN_DEBUG" , "SONO IN MAIN APP" + userViewModel.user.value.toString())

        // VERIFICO CHE L'UTENTE SIA NELLA COLLEZIONE UTENTI DI FIRESTORE
        if (userViewModel.user.value== null)
        {
            // 2 SE LUTENTE NON CE =>
            AddUtente()
            // GLI DICO BENVENUTO HO VERIFICATO CHE SEI NUOOVO DAMMI ALTRE INFORMAZIONI PER PROSEGUIRE
            // E POI LO PORTO ALLA SCHERMATA DI CREA O ADERISIC AD AZIENDA
        }
    }


    // 1 SE L'UTENTE CE =>
    // VERIFICO SUCCESSSIVAMENTE SE HA GIA UN AZIENDA SU CUI ADERISCO OPPURE
    // FACCIO VEDERE LA SCHERMATA CREA O ADERISICI AD AZIENDA



    // SE CREA MI FACCIO DARE TUTIT I DATI PER CREARE L'AZIENDA
    // SE ADERISCE MI PRENDO LA SUA EMAIL E VERIFICO SE QUALCHE AMMINISTRATORE DI QUALCHE AZIENDA HA INVIATO
    // UN EMAIL DI INVITO ALLA SUA EMAIL  E POI GLI DICO SE VUOLE ACCETTARE O RIFIUTARE L'INVITO


    val isCompletedLogin = false

    if(isCompletedLogin) {


        // Usa un LaunchedEffect per nascondere lo SplashScreen dopo un certo tempo
        LaunchedEffect(Unit) {
            delay(2000) // Mostra lo SplashScreen per 2 secondi
            splashScreenViewModel.hideSplash() // Nascondi lo SplashScreen
        }


        // Mostra lo SplashScreen o lo Scaffold in base allo stato
        if (splashScreenViewModel.isSplashVisible.value) {
            SplashScreen()
        } else {
            AppScaffold() // Mostra lo Scaffold dopo lo SplashScreen
        }
    }

}
