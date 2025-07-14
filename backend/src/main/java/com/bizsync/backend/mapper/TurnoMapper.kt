package com.bizsync.backend.mapper

import com.bizsync.backend.dto.TurnoFirestore
import com.bizsync.domain.model.Turno
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun TurnoFirestore.toDomain(documentId: String): Turno {
    return Turno(
        id = documentId,
        nome = nome,
        orarioInizio = orarioInizio,
        orarioFine = orarioFine,
        dipendente = dipendente,
        dipartimentoId = dipartimentoId,
        data = try {
            LocalDate.parse(data)
        } catch (e: Exception) {
            LocalDate.now()
        },
        note = note,
        isConfermato = isConfermato,
        createdAt = createdAt?.toDate()?.toInstant()
            ?.atZone(java.time.ZoneId.systemDefault())
            ?.toLocalDate() ?: LocalDate.now(),
        updatedAt = updatedAt?.toDate()?.toInstant()
            ?.atZone(java.time.ZoneId.systemDefault())
            ?.toLocalDate() ?: LocalDate.now()
    )
}


// Extension functions
fun Turno.toFirestore(): TurnoFirestore {
    return TurnoFirestore(
        nome = nome,
        orarioInizio = orarioInizio,
        orarioFine = orarioFine,
        dipendente = dipendente,
        dipartimentoId = dipartimentoId,
        data = data.format(DateTimeFormatter.ISO_LOCAL_DATE),
        note = note,
        isConfermato = isConfermato,
        createdAt = if (id.isEmpty()) Timestamp.now() else null,
        updatedAt = Timestamp.now()
    )
}