package com.bizsync.app
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.app.navigation.LocalUserViewModel
import com.bizsync.app.screens.AddUtente
import com.bizsync.app.screens.LoginScreen
import com.bizsync.ui.viewmodels.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var currentUserLogin = mutableStateOf(false)


    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            val user = FirebaseAuth.getInstance().currentUser
            if (result.resultCode == RESULT_OK && user != null) {
                currentUserLogin.value = true
                showToast("Login riuscito: ${user.email}")
            } else {
                showToast("Errore login! Codice: ${result.resultCode}")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)


        setContent {

            val navController = rememberNavController()
            val userViewModel : UserViewModel = hiltViewModel()
            val currentUser = FirebaseAuth.getInstance().currentUser

            Log.d("LOGIN_DEBUG", currentUser?.email.toString())
            Log.d("LOGIN_DEBUG", currentUser?.uid.toString())

            if (currentUser == null) {

                // NON SONO LOGGATO VUOL DIRE CHE 2 COSE
                // APRO IL LOGINSCREEN CON TASTO ENTRA
                LoginScreen(onLoginScreen = { startSignIn() },)
            }
            else
            {
                currentUserLogin.value = true
            }


            if ( currentUserLogin.value)
            {
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalUserViewModel provides userViewModel
                ) {
                    MainApp()
                }

            }
        }
    }

    private fun startSignIn() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        signInLauncher.launch(signInIntent)
    }
}



