package com.bizsync.ui.model

import com.bizsync.domain.model.Azienda
import com.bizsync.ui.viewmodels.TimbratureWithUser
import com.bizsync.ui.viewmodels.TodayStats
import com.bizsync.ui.viewmodels.TurnoWithUsers

data class ManagerHomeState(
    val azienda : Azienda = Azienda(),
    val todayStats: TodayStats = TodayStats(),
    val recentTimbrature: List<TimbratureWithUser> = emptyList(),
    val todayShifts: List<TurnoWithUsers> = emptyList(),
    val daysUntilShiftPublication: Int = 0,
    val shiftsPublishedThisWeek: Boolean? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)