package com.bizsync.ui.model

import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.TurnoWithDetails
import com.bizsync.domain.model.User

data class EmployeeHomeState(
    val badge: BadgeVirtuale? = null,
    val prossimoTurno: ProssimoTurno? = null,
    val todayTurno: TurnoWithDetails? = null,
    val timbratureOggi: List<Timbratura> = emptyList(),
    val canTimbra: Boolean = false,
    val daysUntilShiftPublication: Int? = null,
    val shiftsPublishedThisWeek: Boolean = false,
    val isLoading: Boolean = false,
    val isGettingLocation: Boolean = false,
    val error: String? = null,
    val showSuccess: Boolean = false,
    val successMessage: String = "",
    val user: User = User(),
    val azienda: Azienda = Azienda()
)
