package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.AddUtente
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.produceState
import com.bizsync.app.screens.AddAzienda
import com.bizsync.app.screens.ChooseAzienda


@Composable
fun MainApp() {


    val splashVM: SplashViewModel = viewModel()
    val userVM = LocalUserViewModel.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid


    val isCreated = produceState<Boolean?>(initialValue = null, key1 = uid) {
        if (uid != null) {
            value = userVM.checkUser(uid)
        }
    }


    LaunchedEffect(isCreated) {
        if (isCreated.value != null) splashVM.hideSplash()
    }

    when (isCreated.value) {
        null -> SplashScreen()
        false -> OnboardingFlow(onSuccess = {})
        true -> AppScaffold()
    }


}



@Composable
fun OnboardingFlow(onSuccess : () -> Unit)
{
    var splashScreenViewModel : SplashViewModel = viewModel()





    if (splashScreenViewModel.chooseAzienda.value == false) {
        AddUtente(onChooseAzienda = {
            splashScreenViewModel.chooseAzienda.value = true
        })
    } else if (splashScreenViewModel.chooseAzienda.value && !splashScreenViewModel.creaAzienda.value && !splashScreenViewModel.chooseInvito.value) {
        ChooseAzienda(
            onCreaAzienda = { splashScreenViewModel.creaAzienda.value = true },
            onVisualizzaInviti = { splashScreenViewModel.chooseInvito.value = true }
        )
    }
    else if (splashScreenViewModel.chooseAzienda.value && splashScreenViewModel.creaAzienda.value) {
        AddAzienda()
    }
    else if (splashScreenViewModel.chooseAzienda.value && splashScreenViewModel.chooseInvito.value) {
        // VA CREATO
    }
    // 2 SE LUTENTE NON CE =>

    // GLI DICO BENVENUTO HO VERIFICATO CHE SEI NUOOVO DAMMI ALTRE INFORMAZIONI PER PROSEGUIRE

    // E POI LO PORTO ALLA SCHERMATA DI CREA O ADERISIC AD AZIENDA




    // 1 SE L'UTENTE CE =>
    // VERIFICO SUCCESSSIVAMENTE SE HA GIA UN AZIENDA SU CUI ADERISCO OPPURE
    // FACCIO VEDERE LA SCHERMATA CREA O ADERISICI AD AZIENDA


    // SE CREA MI FACCIO DARE TUTIT I DATI PER CREARE L'AZIENDA
    // SE ADERISCE MI PRENDO LA SUA EMAIL E VERIFICO SE QUALCHE AMMINISTRATORE DI QUALCHE AZIENDA HA INVIATO
    // UN EMAIL DI INVITO ALLA SUA EMAIL  E POI GLI DICO SE VUOLE ACCETTARE O RIFIUTARE L'INVITO

    if (splashScreenViewModel.terminate.value )
    {
        onSuccess
    }

}









