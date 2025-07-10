package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.OnboardingNavHost
import com.bizsync.domain.constants.sealedClass.OnboardingScreen
import androidx.compose.runtime.getValue
import com.bizsync.app.screensMore.AppScaffold
import com.bizsync.ui.components.StatusDialog
import kotlinx.coroutines.delay


@Composable
fun MainApp(onLogout : () -> Unit) {
    val splashVM: SplashViewModel = viewModel()
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val check =  userState.checkUser
    val uid = FirebaseAuth.getInstance().currentUser?.uid


    userState.resultMsg?.let { error ->
        StatusDialog(message = error, statusType = com.bizsync.ui.components.DialogStatusType.ERROR, onDismiss = { userVM.clearError()})
    }


    LaunchedEffect(uid) {
        if (uid != null) {
            userVM.checkUser(uid)
        }
    }


    LaunchedEffect(userState.azienda.idAzienda) {
        if (userState.azienda.idAzienda.isNotEmpty()) {
            splashVM.getAllUserByIdAgency(userState.azienda.idAzienda)
            splashVM.getAllContrattiByIdAzienda(userState.azienda.idAzienda)
        }
    }


    when (check) {
        null  -> SplashScreen()
        false -> OnboardingFlow(onSuccess = { userVM.change() })
        true  -> AppScaffold(onLogout)
    }
}



@Composable
fun OnboardingFlow(onSuccess : () -> Unit)
{
    var userVM = LocalUserViewModel.current

    val userState = userVM.uiState.collectAsState()
    val uid = userState.value.user.uid
    val navController = rememberNavController()

    if(uid.isEmpty()){
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.AddUtente.route)
    }
    else

    {
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.ChooseAzienda.route)

    }

}









