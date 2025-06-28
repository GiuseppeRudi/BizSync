package com.bizsync.ui.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.bizsync.domain.constants.enumClass.AbsenceType
import com.bizsync.ui.model.AbsenceTypeUi

fun AbsenceType.toUiData(): AbsenceTypeUi = when (this) {
    AbsenceType.VACATION -> AbsenceTypeUi(
        type = this,
        displayName = "Ferie",
        icon = Icons.Default.BeachAccess,
        color = Color(0xFF4CAF50),
        requiresApproval = true
    )
    AbsenceType.ROL -> AbsenceTypeUi(
        type = this,
        displayName = "Permessi ROL",
        icon = Icons.Default.Schedule,
        color = Color(0xFF2196F3),
        requiresApproval = true
    )
    AbsenceType.PERSONAL_LEAVE -> AbsenceTypeUi(
        type = this,
        displayName = "Permessi Personali",
        icon = Icons.Default.Person,
        color = Color(0xFF9C27B0),
        requiresApproval = true
    )
    AbsenceType.SICK_LEAVE -> AbsenceTypeUi(
        type = this,
        displayName = "Malattia",
        icon = Icons.Default.LocalHospital,
        color = Color(0xFFF44336),
        requiresApproval = false
    )
    AbsenceType.WORK_INJURY -> AbsenceTypeUi(
        type = this,
        displayName = "Infortunio Lavoro",
        icon = Icons.Default.Warning,
        color = Color(0xFFFF5722),
        requiresApproval = false
    )
    AbsenceType.MATERNITY -> AbsenceTypeUi(
        type = this,
        displayName = "Maternità",
        icon = Icons.Default.ChildCare,
        color = Color(0xFFE91E63),
        requiresApproval = false
    )
    AbsenceType.PATERNITY -> AbsenceTypeUi(
        type = this,
        displayName = "Paternità",
        icon = Icons.Default.FamilyRestroom,
        color = Color(0xFF3F51B5),
        requiresApproval = false
    )
    AbsenceType.LAW_104 -> AbsenceTypeUi(
        type = this,
        displayName = "Permessi L.104",
        icon = Icons.Default.Accessible,
        color = Color(0xFF607D8B),
        requiresApproval = false
    )
    AbsenceType.UNPAID_LEAVE -> AbsenceTypeUi(
        type = this,
        displayName = "Non Retribuiti",
        icon = Icons.Default.MoneyOff,
        color = Color(0xFF795548),
        requiresApproval = true
    )
    AbsenceType.TRAINING -> AbsenceTypeUi(
        type = this,
        displayName = "Formazione/Studio",
        icon = Icons.Default.School,
        color = Color(0xFF00BCD4),
        requiresApproval = true
    )
    AbsenceType.STRIKE -> AbsenceTypeUi(
        type = this,
        displayName = "Sciopero",
        icon = Icons.Default.Campaign,
        color = Color(0xFFFF9800),
        requiresApproval = false
    )
}
