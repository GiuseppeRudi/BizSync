package com.bizsync.cache.mapper

import com.bizsync.cache.entity.CcnlnfoEntity
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.domain.model.Ccnlnfo
import com.bizsync.domain.model.Contratto

object ContrattoEntityMapper {

    fun toDomain(entity: ContrattoEntity): Contratto {
        return Contratto(
            id = entity.id,
            idDipendente = entity.idDipendente,
            idAzienda = entity.idAzienda,
            emailDipendente = entity.emailDipendente,
            posizioneLavorativa = entity.posizioneLavorativa,
            dipartimento = entity.dipartimento,
            tipoContratto = entity.tipoContratto,
            oreSettimanali = entity.oreSettimanali,
            settoreAziendale = entity.settoreAziendale,
            dataInizio = entity.dataInizio,
            ccnlInfo = Ccnlnfo(
                settore = entity.ccnlInfo.settore,
                ruolo = entity.ccnlInfo.ruolo,
                ferieAnnue = entity.ccnlInfo.ferieAnnue,
                rolAnnui = entity.ccnlInfo.rolAnnui,
                stipendioAnnualeLordo = entity.ccnlInfo.stipendioAnnualeLordo,
                malattiaRetribuita = entity.ccnlInfo.malattiaRetribuita
            ) ,
            ferieUsate = entity.ferieUsate,
            rolUsate = entity.rolUsate,
            malattiaUsata = entity.malattiaUsata

        )
    }

    fun toEntity(domain: Contratto): ContrattoEntity {
        return ContrattoEntity(
            id = domain.id,
            idDipendente = domain.idDipendente,
            idAzienda = domain.idAzienda,
            emailDipendente = domain.emailDipendente,
            posizioneLavorativa = domain.posizioneLavorativa,
            dipartimento = domain.dipartimento,
            tipoContratto = domain.tipoContratto,
            oreSettimanali = domain.oreSettimanali,
            settoreAziendale = domain.settoreAziendale,
            dataInizio = domain.dataInizio,
            ccnlInfo = CcnlnfoEntity(
                settore = domain.ccnlInfo.settore,
                ruolo = domain.ccnlInfo.ruolo,
                ferieAnnue = domain.ccnlInfo.ferieAnnue,
                rolAnnui = domain.ccnlInfo.rolAnnui,
                stipendioAnnualeLordo = domain.ccnlInfo.stipendioAnnualeLordo,
                malattiaRetribuita = domain.ccnlInfo.malattiaRetribuita
            ),
            ferieUsate = domain.ferieUsate,
            rolUsate = domain.rolUsate,
            malattiaUsata = domain.malattiaUsata
        )
    }
}

fun ContrattoEntity.toDomain(): Contratto {
    return ContrattoEntityMapper.toDomain(this)
}

fun Contratto.toEntity(): ContrattoEntity {
    return ContrattoEntityMapper.toEntity(this)
}

fun List<ContrattoEntity>.toDomainList(): List<Contratto> {
    return this.map { it.toDomain() }
}

fun List<Contratto>.toEntityList(): List<ContrattoEntity> {
    return this.map { it.toEntity() }
}
