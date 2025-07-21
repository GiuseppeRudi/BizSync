package com.bizsync.cache.mapper


import com.bizsync.cache.entity.TimbraturaEntity
import com.bizsync.domain.model.Timbratura

fun Timbratura.toEntity(): TimbraturaEntity {
    return TimbraturaEntity(
        id = id,
        idTurno = idTurno,
        idDipendente = idDipendente,
        idAzienda = idAzienda,
        idFirebase = idFirebase,
        tipoTimbratura = tipoTimbratura,
        dataOraTimbratura = dataOraTimbratura,
        dataOraPrevista = dataOraPrevista,
        zonaLavorativa = zonaLavorativa,
        posizioneVerificata = posizioneVerificata,
        distanzaDallAzienda = distanzaDallAzienda,
        dentroDellaTolleranza = dentroDellaTolleranza,
        statoTimbratura = statoTimbratura,
        minutiRitardo = minutiRitardo,
        note = note,
        verificataDaManager = verificataDaManager,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimbraturaEntity.toDomain(): Timbratura {
    return Timbratura(
        id = id,
        idTurno = idTurno,
        idDipendente = idDipendente,
        idAzienda = idAzienda,
        idFirebase = idFirebase,
        tipoTimbratura = tipoTimbratura,
        dataOraTimbratura = dataOraTimbratura,
        dataOraPrevista = dataOraPrevista,
        zonaLavorativa = zonaLavorativa,
        posizioneVerificata = posizioneVerificata,
        distanzaDallAzienda = distanzaDallAzienda,
        dentroDellaTolleranza = dentroDellaTolleranza,
        statoTimbratura = statoTimbratura,
        minutiRitardo = minutiRitardo,
        note = note,
        verificataDaManager = verificataDaManager,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<TimbraturaEntity>.toDomainList(): List<Timbratura> = map { it.toDomain() }
fun List<Timbratura>.toEntityList(): List<TimbraturaEntity> = map { it.toEntity() }
