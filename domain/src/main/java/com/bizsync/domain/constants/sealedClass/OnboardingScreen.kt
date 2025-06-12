package com.bizsync.domain.constants.sealedClass

sealed class OnboardingScreen(val route: String) {
    object AddUtente : OnboardingScreen("add_utente")
    object ChooseAzienda : OnboardingScreen("choose_azienda")
    object CreaAzienda : OnboardingScreen("crea_azienda")
    object ChooseInvito : OnboardingScreen("choose_invito")
    object Terminate : OnboardingScreen("terminate")
}
