package com.bizsync.domain.usecases

import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.domain.repository.BadgeRepository
import javax.inject.Inject

class CreaTimbrnaturaUseCase @Inject constructor(
    private val badgeService: BadgeRepository
) {
    suspend operator fun invoke(
        turno: Turno,
        dipendente: User,
        azienda: Azienda,
        tipoTimbratura: TipoTimbratura,
        latitudine: Double?,
        longitudine: Double?
    ): Resource<Timbratura> {
        return badgeService.creaTimbratura(turno, dipendente, azienda, tipoTimbratura, latitudine, longitudine)
    }
}