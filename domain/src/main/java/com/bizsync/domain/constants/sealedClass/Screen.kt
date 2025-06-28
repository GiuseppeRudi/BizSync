package com.bizsync.domain.constants.sealedClass

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Turni    : Screen("turni")
    object Chat     : Screen("chat")
    object Gestione : Screen("gestione")
    object Grafici  : Screen("grafici")
}
