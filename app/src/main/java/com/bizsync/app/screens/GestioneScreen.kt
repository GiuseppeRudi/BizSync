package com.bizsync.app.screens

import android.widget.Button
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.bizsync.ui.components.MakeInviteDialog
import com.bizsync.ui.viewmodels.GestioneViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.app.navigation.LocalUserViewModel

@Composable
fun GestioneScreen()
{
    val gestioneVM : GestioneViewModel = hiltViewModel()
    val showInviteDialog by gestioneVM.showDialog.collectAsState()
    val userVM = LocalUserViewModel.current

    Box(modifier= Modifier.fillMaxSize())
    {
        Text(text = "Gestione Screen")
    }

    Button(onClick = { gestioneVM.onShowDialogChanged(true)} ) { }

    MakeInviteDialog(showInviteDialog, onDismiss = {gestioneVM.onShowDialogChanged(false)}, userVM )
}
