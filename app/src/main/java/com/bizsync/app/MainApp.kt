package com.bizsync.app

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.AddUtente
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.produceState
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.OnboardingNavHost
import com.bizsync.app.navigation.sealedClass.OnboardingScreen
import com.bizsync.app.screens.AddAzienda
import com.bizsync.app.screens.ChooseAzienda
import com.bizsync.app.screens.ChooseInvito
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec


@Composable
fun MainApp() {
    val splashVM: SplashViewModel = viewModel()
    val userVM = LocalUserViewModel.current
    val check =  userVM.check.collectAsState(initial = null)
    val uid = FirebaseAuth.getInstance().currentUser?.uid


    //2) al lancio (o al cambio di uid) lo imposto dal repo
    LaunchedEffect(uid) {
        if (uid != null) {
            userVM.checkUser(uid)
            splashVM.hideSplash()
        }
    }

    //3) branching su null/false/true
    when (check.value) {
        null  -> SplashScreen()
        false -> OnboardingFlow(onSuccess = { userVM.change() })
        true  -> AppScaffold()
    }
}



@Composable
fun OnboardingFlow(onSuccess : () -> Unit)
{
    var userVM = LocalUserViewModel.current

    val navController = rememberNavController()

    if(userVM.user.value == null){
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.AddUtente.route)
    }
    else
    {
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.ChooseAzienda.route)

    }

}









