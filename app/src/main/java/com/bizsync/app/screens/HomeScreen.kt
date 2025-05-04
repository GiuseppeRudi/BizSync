package com.bizsync.app.screens



import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.components.ErrorDialog
import com.bizsync.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun HomeScreen() {
    val userviewmodel : UserViewModel = hiltViewModel()
    val navController = LocalNavController.current

    val currentUser = FirebaseAuth.getInstance().currentUser

    val errore = remember {mutableStateOf<String?>("errore")}

    userviewmodel.onUidChanged(currentUser?.uid.toString())


    Button(onClick = { navController.navigate("pianifica")} ) { }

    ErrorDialog(errore.value, onDismiss = { errore.value= null})

}









