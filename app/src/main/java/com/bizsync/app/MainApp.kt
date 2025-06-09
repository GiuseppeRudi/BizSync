package com.bizsync.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.AppScaffold
import com.bizsync.app.screens.SplashScreen
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.OnboardingNavHost
import com.bizsync.model.sealedClass.OnboardingScreen
import androidx.compose.runtime.getValue


@Composable
fun MainApp(onLogout : () -> Unit) {
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
        true  -> AppScaffold(onLogout)
    }
}



@Composable
fun OnboardingFlow(onSuccess : () -> Unit)
{
    var userVM = LocalUserViewModel.current

    val user by userVM.user.collectAsState()
    val navController = rememberNavController()

    if(user.uid.isEmpty()){
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.AddUtente.route)
    }
    else

    {
        OnboardingNavHost(navController, onSuccess,OnboardingScreen.ChooseAzienda.route)

    }

}









