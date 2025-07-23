package com.bizsync.app

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.OnboardingNavHost
import com.bizsync.domain.constants.sealedClass.OnboardingScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bizsync.app.screens.SplashScreenWithProgress
import com.bizsync.app.screensMore.AppScaffold
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.components.StatusDialog
import kotlinx.coroutines.delay

@Composable
fun MainApp(onLogout: () -> Unit) {
    val splashVM: SplashViewModel = viewModel()
    val userVM = LocalUserViewModel.current
    val userState by userVM.uiState.collectAsState()
    val check = userState.checkUser
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // 🔥 TIMER SEMPLICE: Si resetta automaticamente ad ogni ricomposizione
    var showingSplash by remember(check, uid) { mutableStateOf(check == null) }

    userState.resultMsg?.let { error ->
        StatusDialog(
            message = error,
            statusType = DialogStatusType.ERROR,
            onDismiss = { userVM.clearError() }
        )
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            userVM.checkUser(uid)
        }
    }

    LaunchedEffect(userState.azienda.idAzienda, userState.user.isManager) {
        if (userState.azienda.idAzienda.isNotEmpty() && userState.user.isManager && userState.user.uid.isNotEmpty()) {
            splashVM.getAllUserByIdAgency(userState.azienda.idAzienda, userState.user.uid)
            splashVM.getAllContrattiByIdAzienda(userState.azienda.idAzienda)
            splashVM.getAllAbsencesByIdAzienda(userState.azienda.idAzienda)
            splashVM.getAllTurniByIdAzienda(userState.azienda.idAzienda)
        }

        if (userState.azienda.idAzienda.isNotEmpty() && !userState.user.isManager && userState.user.uid.isNotEmpty()) {
            splashVM.getAllUserByIdAgency(userState.azienda.idAzienda, userState.user.uid)
            splashVM.getAllAbsencesByIdAzienda(userState.azienda.idAzienda, userState.user.uid)
            splashVM.getAllTurniByIdAzienda(userState.azienda.idAzienda, userState.user.uid)
        }

        splashVM.clearObsoleteCacheIfNeeded()
    }

    // ⏱️ TIMER MINIMO SOLO QUANDO I DATI SONO PRONTI
    LaunchedEffect(check) {
        if (check != null && showingSplash) {
            Log.d("SPLASH_TIMING", "⏳ Dati , attendo 1.5s per animazione")
            delay(1500) // Timer ridotto per sviluppo
            showingSplash = false
            Log.d("SPLASH_TIMING", "✅ Timer completato")
        }
    }

    // 🎭 NAVIGAZIONE DIRETTA
    when {
        showingSplash -> {
            SplashScreenWithProgress(
                isDataReady = check != null,
                elapsedTime = 0L,
                minimumDuration = 1500L
            )
        }
        check == false -> OnboardingFlow(onSuccess = { userVM.change() })
        check == true -> AppScaffold(onLogout)
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









