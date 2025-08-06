package com.bizsync.domain.repository

import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.User
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.Turno
import java.time.LocalDate

interface BadgeRepository {
    suspend fun createBadgeVirtuale(user: User, azienda: Azienda): BadgeVirtuale

    suspend fun getTimbratureGiornaliere(userId: String, date: LocalDate): Resource<List<Timbratura>>
    suspend fun getProssimoTurno(userId: String): Resource<ProssimoTurno>
    suspend fun creaTimbratura(
        turno: Turno,
        dipendente: User,
        azienda: Azienda,
        tipoTimbratura: TipoTimbratura,
        latitudine: Double?,
        longitudine: Double?
    ): Resource<Timbratura>
}