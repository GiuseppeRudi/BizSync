package com.bizsync.model.sealedClass

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Pianifica: Screen("pianifica")
    object Chat     : Screen("chat")
    object Gestione : Screen("gestione")
    object Grafici  : Screen("grafici")
}
