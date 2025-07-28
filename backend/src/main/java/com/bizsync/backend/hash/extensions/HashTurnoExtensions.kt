package com.bizsync.backend.hash.extensions


import com.bizsync.backend.hash.HashManager
import com.bizsync.cache.entity.TurnoEntity
import com.bizsync.domain.model.Turno
import com.bizsync.cache.mapper.toDomain

// Hash per singolo turno
fun Turno.generateTurnoHash(): String {
    val turnoString = listOf(
        id,
        titolo,
        idAzienda,
        idFirebase,
        idDipendenti.joinToString(","),
        orarioInizio.toString(),
        orarioFine.toString(),
        dipartimento,
        data.toString(),
        createdAt.toString(),
        updatedAt.toString()
    ).joinToString("|")

    return HashManager.generateHash(turnoString)
}



// Hash da Entity list (Room)
fun List<TurnoEntity>.generateCacheHash(): String {
    val domainTurni = this.map { it.toDomain() }
    return generateDomainTurniHash(domainTurni)
}

// Hash da domain list
fun List<Turno>.generateDomainHash(): String {
    return generateDomainTurniHash(this)
}

private fun generateDomainTurniHash(turni: List<Turno>): String {
    return HashManager.generateHashFromList(turni) { it.generateTurnoHash() }
}
