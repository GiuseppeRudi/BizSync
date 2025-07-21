package com.bizsync.ui.mapper

import com.bizsync.domain.model.Azienda
import com.bizsync.ui.model.AziendaUi

object AziendaUiMapper {

    fun toDomain(ui: AziendaUi): Azienda {
        return Azienda(
            idAzienda = ui.idAzienda,
            nome = ui.nome,
            areeLavoro = ui.areeLavoro,
            turniFrequenti = ui.turniFrequenti,
            numDipendentiRange = ui.numDipendentiRange,
            sector = ui.sector,
            giornoPubblicazioneTurni = ui.giornoPubblicazioneTurni,
            // mapping coordinate azienda
            latitudine = ui.latitudine,
            longitudine = ui.longitudine,
            tolleranzaMetri = ui.tolleranzaMetri
        )
    }

    fun toUi(domain: Azienda): AziendaUi {
        return AziendaUi(
            idAzienda = domain.idAzienda,
            nome = domain.nome,
            areeLavoro = domain.areeLavoro,
            turniFrequenti = domain.turniFrequenti,
            numDipendentiRange = domain.numDipendentiRange,
            sector = domain.sector,
            giornoPubblicazioneTurni = domain.giornoPubblicazioneTurni,
            // mapping coordinate azienda
            latitudine = domain.latitudine,
            longitudine = domain.longitudine,
            tolleranzaMetri = domain.tolleranzaMetri
        )
    }

    fun toUiList(domains: List<Azienda>): List<AziendaUi> = domains.map { toUi(it) }
    fun toDomainList(uis: List<AziendaUi>): List<Azienda> = uis.map { toDomain(it) }
}

// Extension functions for convenience
fun Azienda.toUi(): AziendaUi = AziendaUiMapper.toUi(this)
fun AziendaUi.toDomain(): Azienda = AziendaUiMapper.toDomain(this)
fun List<Azienda>.toUiList(): List<AziendaUi> = AziendaUiMapper.toUiList(this)
fun List<AziendaUi>.toDomainList(): List<Azienda> = AziendaUiMapper.toDomainList(this)
