package com.bizsync.ui.mapper


import com.bizsync.domain.model.Azienda
import com.bizsync.ui.model.AziendaUi

object AziendaUiMapper {

    fun toUiState(domain: Azienda): AziendaUi {
        return AziendaUi(
            idAzienda = domain.idAzienda,
            nome = domain.nome,
            areeLavoro = domain.areeLavoro,
            turniFrequenti = domain.turniFrequenti,
            isSelezionata = false,
            isEspansa = false,
            nomeErrore = null,
            numeroDipendenti = 0,
            descrizioneAzienda = ""
        )
    }

    fun toDomain(ui: AziendaUi): Azienda {
        return Azienda(
            idAzienda = ui.idAzienda,
            nome = ui.nome,
            areeLavoro = ui.areeLavoro,
            turniFrequenti = ui.turniFrequenti
        )
    }

    fun toUiStateList(domains: List<Azienda>): List<AziendaUi> {
        return domains.map { toUiState(it) }
    }
}

// Estensioni
fun Azienda.toUiState(): AziendaUi = AziendaUiMapper.toUiState(this)
fun AziendaUi.toDomain(): Azienda = AziendaUiMapper.toDomain(this)
fun List<Azienda>.toUiStateList(): List<AziendaUi> = AziendaUiMapper.toUiStateList(this)
