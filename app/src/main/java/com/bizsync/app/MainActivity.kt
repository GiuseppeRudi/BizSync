package com.bizsync.app
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {

    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private var isUserLoggedIn by mutableStateOf(false)

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            val user = FirebaseAuth.getInstance().currentUser
            if (result.resultCode == RESULT_OK && user != null) {
                isUserLoggedIn = true // ✅ Utente autenticato
                showToast("Login riuscito: ${user.email}")
            } else {
                showToast("Errore login! Codice: ${result.resultCode}")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this) // ✅ Inizializza Firebase

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startSignIn()
        }
        else
            isUserLoggedIn = true // ✅ Utente già autenticato

        setContent {
            if (isUserLoggedIn) {
                AppNavigator()
            } else {
                Text("Attendere...") // Mostra un messaggio di attesa
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

        signInLauncher.launch(signInIntent) // ✅ Nuovo modo per gestire i risultati
    }
}


