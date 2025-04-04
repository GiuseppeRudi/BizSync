package com.bizsync.app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.bizsync.ui.components.Calendar




import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items

import androidx.lifecycle.viewmodel.compose.viewModel
import com.bizsync.ui.components.DialogAddShif
import com.bizsync.ui.components.RoundedButton
import com.bizsync.ui.viewmodels.CalendarViewModel
import com.bizsync.ui.viewmodels.DialogAddShiftViewModel

@Composable
fun PianificaScreen() {

    val dialogviewmodel : DialogAddShiftViewModel = viewModel()
    val calendarviewmodel : CalendarViewModel = viewModel()

    Calendar()


    // Lista degli elementi
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(dialogviewmodel.itemsList) { item ->  // ✅ Qui passa direttamente la lista
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
        }
    }


    Column(    modifier = Modifier
        .fillMaxSize() // Occupa tutto lo spazio disponibile
        .padding(16.dp),
        horizontalAlignment = Alignment.End,  // Allinea orizzontalmente a destra
        verticalArrangement = Arrangement.Bottom // Posiziona in basso
    ) {     RoundedButton(true, onShow = { calendarviewmodel.showDialogShift.value = true })
    }

    DialogAddShif(showDialog= calendarviewmodel.showDialogShift.value, onDismiss = { calendarviewmodel.showDialogShift.value = false })

}



@Preview
@Composable
fun PianificaScreenPreview() {
    PianificaScreen()
}
