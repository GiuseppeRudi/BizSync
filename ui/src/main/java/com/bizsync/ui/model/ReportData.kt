package com.bizsync.ui.model

import com.bizsync.domain.model.Absence
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User

data class ReportData(
    val contratti: List<Contratto>,
    val users: List<User>,
    val absences: List<Absence>,
    val turni: List<Turno>
)
