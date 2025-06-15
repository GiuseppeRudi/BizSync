// MainActivity.kt
package com.bizsync.app

import LoginScreen
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.ui.viewmodels.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // stato di login
    private val currentUserLogin = mutableStateOf(false)

    // launcher per FirebaseUI
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        val user = FirebaseAuth.getInstance().currentUser
        if (result.resultCode == RESULT_OK && user != null) {
            currentUserLogin.value = true
            showToast("Login riuscito: ${user.email}")
        } else {
            showToast("Login annullato o fallito")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )


        //PER FARE DI DEPLOY
//        FirebaseAppCheck.getInstance()
//            .installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance()
//            )


        setContent {
            val navController = rememberNavController()
            val userViewModel: UserViewModel = hiltViewModel()
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            // Se già loggato, salto al MainApp
            if (firebaseUser != null) {
                currentUserLogin.value = true
            }

            if (!currentUserLogin.value) {
                LoginScreen(onLogin = { launchLoginFlow() })
            }
            else {
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalUserViewModel provides userViewModel
                ) {
                    MainApp(onLogout = { userViewModel.clear()
                                            performLogout() })
                }
            }

        }
    }

    /** Avvia FirebaseUI con Smart Lock + auto-sign-in */
    private fun launchLoginFlow() {
        val providers = listOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            // enableCredentials = true per salvare in Smart Lock
            // enableAutoSignIn = true per auto-login senza UI
            .setIsSmartLockEnabled(true)
            .build()
        signInLauncher.launch(intent)
    }

    /** Logout completo (Firebase + Smart Lock) */
    private fun performLogout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                currentUserLogin.value = false
                showToast("Logout effettuato")
            }



    }

    /** Helper per toast */
    private fun Context.showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
