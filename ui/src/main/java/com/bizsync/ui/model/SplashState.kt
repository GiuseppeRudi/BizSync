package com.bizsync.ui.model

import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.ui.components.DialogStatusType
import java.time.LocalDate

data class SplashState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val contratti: List<Contratto> = emptyList(),
    val absence: List<AbsenceUi> = emptyList(),
    val turni: Map<LocalDate, List<Turno>> = emptyMap(),
    val errorMsg: String? = null,
    val statusType: DialogStatusType = DialogStatusType.ERROR,
    val syncInProgress: Boolean = false,
    val lastSyncTime: Long? = null
)