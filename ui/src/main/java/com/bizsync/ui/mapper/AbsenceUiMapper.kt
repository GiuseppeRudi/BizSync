package com.bizsync.ui.mapper

import com.bizsync.domain.model.Absence
import com.bizsync.ui.model.AbsenceUi
import java.time.LocalDate
import com.bizsync.ui.mapper.toUiData as typeToUiData
import com.bizsync.ui.mapper.toUiData as statusToUiData

import java.time.format.DateTimeFormatter

fun Absence.toUi(): AbsenceUi {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    return AbsenceUi(
        id = id,
        typeUi = type.typeToUiData(),

        startDate = startDate,
        endDate = endDate,

        startTime = startTime,
        endTime = endTime,

        totalDays = "", // opzionale: puoi calcolarlo nella ViewModel se vuoi un valore preciso

        reason = reason,
        statusUi = status.statusToUiData(),
        approver = approvedBy,

        submittedOn = submittedDate.format(dateFormatter),
        submittedDate = submittedDate,

        approvedDate = approvedDate,
        comments = comments
    )
}





fun AbsenceUi.toDomain(): Absence {
    requireNotNull(startDate) { "startDate non deve essere nullo" }
    requireNotNull(endDate) { "endDate non deve essere nullo" }

    return Absence(
        id = id,
        type = typeUi.type,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        reason = reason,
        status = statusUi.status,
        submittedDate = submittedDate ?: LocalDate.now(),
        approvedBy = approver,
        approvedDate = approvedDate,
        comments = comments
    )
}



