package com.bizsync.backend.hash.extensions


import com.bizsync.backend.hash.HashManager
import com.bizsync.cache.entity.ContrattoEntity
import com.bizsync.cache.mapper.toDomain
import com.bizsync.domain.model.Contratto

// Hash singolo contratto
fun Contratto.generateContrattoHash(): String {
    val contrattoString = listOf(
        id,
        idDipendente,
        idAzienda,
        emailDipendente,
        posizioneLavorativa,
        dipartimento,
        tipoContratto,
        oreSettimanali,
        settoreAziendale,
        dataInizio,
        ccnlInfo.settore,
        ccnlInfo.ruolo,
        ccnlInfo.ferieAnnue.toString(),
        ccnlInfo.rolAnnui.toString(),
        ccnlInfo.stipendioAnnualeLordo.toString(),
        ccnlInfo.malattiaRetribuita.toString(),

        ferieUsate.toString(),
        rolUsate.toString(),
        malattiaUsata.toString()
    ).joinToString("|")

    return HashManager.generateHash(contrattoString)
}



// Per Cache (Entity -> Domain -> Hash)
fun List<ContrattoEntity>.generateCacheHash(): String {
    val domainList = this.map { it.toDomain() }
    return generateDomainContrattiHash(domainList)
}

// Diretto da Domain
fun List<Contratto>.generateDomainHash(): String {
    return generateDomainContrattiHash(this)
}

private fun generateDomainContrattiHash(contratti: List<Contratto>): String {
    return HashManager.generateHashFromList(contratti) { it.generateContrattoHash() }
}
