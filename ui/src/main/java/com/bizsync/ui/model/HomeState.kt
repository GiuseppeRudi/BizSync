package com.bizsync.ui.model

import com.bizsync.domain.constants.enumClass.HomeScreenRoute
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.model.Timbratura

data class HomeState(
    val badge: BadgeVirtuale? = null,
    val user: UserUi = UserUi(),
    val azienda: AziendaUi = AziendaUi(),
    val prossimoTurno: ProssimoTurno? = null,
    val canTimbra: Boolean = false,
    val timbratureOggi: List<Timbratura> = emptyList(),
    val lastTimbratura: Timbratura? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = "",
    val currentScreen: HomeScreenRoute = HomeScreenRoute.Home
)