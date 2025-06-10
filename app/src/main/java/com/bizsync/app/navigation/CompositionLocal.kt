package com.bizsync.app.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.bizsync.ui.viewmodels.ScaffoldViewModel
import com.bizsync.ui.viewmodels.UserViewModel

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}

val LocalUserViewModel = staticCompositionLocalOf<UserViewModel> {
    error("UserViewModel not provided")
}

val LocalScaffoldViewModel = staticCompositionLocalOf<ScaffoldViewModel> {
    error("ScaffoldViewModel not provided")
}


