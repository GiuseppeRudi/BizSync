package com.bizsync.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.bizsync.ui.viewmodels.ChatScreenViewModel
import androidx.compose.runtime.getValue

@Composable
fun ChatScreen() {

    val viewModel = ChatScreenViewModel()

//    LazyColumn {
//        if (isLoading) {
//            // Mostra 3 shimmer card placeholder
//            items(3) {
//                ShiftCard(loading = true)
//            }
//        } else {
//            items(shifts) { shift ->
//                AnimatedVisibility(
//                    visible = true,
//                    enter = fadeIn() + slideInVertically(),
//                    exit = fadeOut()
//                ) {
//                    ShiftCard(
//                        loading = false,
//                        title = shift.title,
//                        time = shift.time
//                    )
//                }
//            }
//        }
//    }

}
