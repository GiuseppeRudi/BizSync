package com.bizsync.app.screens



import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalNavController
import com.bizsync.ui.components.Calendar
import com.bizsync.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun HomeScreen() {
    val userviewmodel : UserViewModel = hiltViewModel()
    val navController = LocalNavController.current

    val currentUser = FirebaseAuth.getInstance().currentUser

    userviewmodel.uid.value = currentUser?.uid.toString()

    Log.d("LOGIN_DEBUG", userviewmodel.uid.value.toString())

    Button(onClick = { navController.navigate("pianifica")} ) { }


}









