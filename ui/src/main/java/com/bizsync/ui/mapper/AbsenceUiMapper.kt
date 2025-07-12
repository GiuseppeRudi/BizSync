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

        submittedName = submittedName,
        idUser = idUser,
        idAzienda = idAzienda,

        startDate = startDate,
        endDate = endDate,

        startTime = startTime,
        endTime = endTime,


        reason = reason,
        statusUi = status.statusToUiData(),
        approver = approvedBy,

        submittedDate = submittedDate,

        approvedDate = approvedDate,
        comments = comments,
        totalHours = totalHours,
        totalDays = totalDays
    )
}





fun AbsenceUi.toDomain(): Absence {
    requireNotNull(startDate) { "startDate non deve essere nullo" }
    requireNotNull(endDate) { "endDate non deve essere nullo" }

    return Absence(
        id = id,
        idUser = idUser,
        idAzienda = idAzienda,
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
        comments = comments,
        submittedName = submittedName ,
        totalHours = totalHours,
        totalDays = totalDays
    )
}



