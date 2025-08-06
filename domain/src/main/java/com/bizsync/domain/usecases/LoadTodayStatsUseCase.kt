package com.bizsync.domain.usecases

import android.util.Log
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.DipartimentoInfo
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.TodayStats
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TimbraturaLocalRepository
import com.bizsync.domain.repository.TurnoLocalRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class LoadTodayStatsUseCase @Inject constructor(
    private val turnoLocalRepository: TurnoLocalRepository,
    private val timbraturaLocalRepository: TimbraturaLocalRepository
) {
    suspend operator fun invoke(azienda: Azienda): Resource<TodayStats> {
        return try {
            val today = LocalDate.now()

            // Prendi tutti i turni di oggi
            val todayShifts = turnoLocalRepository.getTurniByDate(today).first()

            val startOfDay = today.atStartOfDay().toString()
            val endOfDay = today.atTime(23, 59, 59).toString()

            // Prendi tutte le timbrature di oggi
            val todayTimbrature = timbraturaLocalRepository.getTimbratureByDate(startOfDay, endOfDay).first()

            // ✅ Business logic spostata nel Use Case
            val stats = calculateSimplifiedTodayStats(todayShifts, todayTimbrature, azienda)

            Resource.Success(stats)
        } catch (e: Exception) {
            Resource.Error("Errore nel calcolo delle statistiche: ${e.message}")
        }
    }

    private fun calculateSimplifiedTodayStats(
        todayShifts: List<Turno>,
        todayTimbrature: List<Timbratura>,
        azienda: Azienda
    ): TodayStats {
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek

        Log.d("TodayStats", " Calcolo statistiche per il giorno: $today ($dayOfWeek)")

        val turniTotaliAssegnati = todayShifts.size
        Log.d("TodayStats", " Turni totali assegnati oggi: $turniTotaliAssegnati")

        // UTENTI ATTIVI OGGI (con turni assegnati)
        val utentiAttiviOggi = todayShifts
            .flatMap { it.idDipendenti }
            .distinct()
            .size
        Log.d("TodayStats", " Utenti attivi oggi (con turni): $utentiAttiviOggi")

        // Dipartimenti con turni e orari (usando orari dell'azienda)
        val dipartimentiConTurni = todayShifts
            .groupBy { it.dipartimento }
            .mapValues { (dipartimento, turni) ->

                Log.d("DipartimentoCheck", " Analizzo dipartimento: $dipartimento con ${turni.size} turni")

                val areaLavoro = azienda.areeLavoro.find { it.nomeArea == dipartimento }

                if (areaLavoro == null) {
                    Log.w("DipartimentoCheck", "Nessuna areaLavoro trovata per: $dipartimento")
                } else {
                    Log.d("DipartimentoCheck", " Area trovata: ${areaLavoro.nomeArea}")
                }

                val orari = areaLavoro?.orariSettimanali?.get(dayOfWeek)
                if (orari == null) {
                    Log.w("OrariCheck", " Nessun orario per $dayOfWeek in area: ${areaLavoro?.nomeArea}")
                } else {
                    Log.d("OrariCheck", " Orari per $dayOfWeek: apertura=${orari.first}, chiusura=${orari.second}")
                }

                DipartimentoInfo(
                    nome = areaLavoro?.nomeArea ?: dipartimento,
                    orarioApertura = orari?.first?.toString() ?: "N/A",
                    orarioChiusura = orari?.second?.toString() ?: "N/A",
                    numeroTurni = turni.size
                )
            }

        val dipartimentiAperti = dipartimentiConTurni.size
        val dipartimentiDetails = dipartimentiConTurni.values.toList()
        Log.d("TodayStats", "🏢 Dipartimenti aperti oggi: $dipartimentiAperti")

        val turniConStato = todayShifts.map { turno ->
            val timbratureTurno = todayTimbrature.filter { it.idTurno == turno.id }
            val haEntrata = timbratureTurno.any { it.tipoTimbratura == TipoTimbratura.ENTRATA }
            val haUscita = timbratureTurno.any { it.tipoTimbratura == TipoTimbratura.USCITA }

            val stato = when {
                haEntrata && haUscita -> "COMPLETATO"
                haEntrata && !haUscita -> "INIZIATO"
                else -> "DA_INIZIARE"
            }

            Log.d("TurniStato", "📊 Turno ID=${turno.id}: $stato")
            stato
        }

        val turniCompletati = turniConStato.count { it == "COMPLETATO" }
        val turniIniziati = turniConStato.count { it == "INIZIATO" }
        val turniDaIniziare = turniConStato.count { it == "DA_INIZIARE" }

        Log.d("TodayStats", " Turni completati: $turniCompletati")
        Log.d("TodayStats", " Turni iniziati: $turniIniziati")
        Log.d("TodayStats", " Turni da iniziare: $turniDaIniziare")

        return TodayStats(
            turniTotaliAssegnati = turniTotaliAssegnati,
            dipartimentiAperti = dipartimentiAperti,
            utentiAttiviOggi = utentiAttiviOggi,
            turniCompletati = turniCompletati,
            turniIniziati = turniIniziati,
            turniDaIniziare = turniDaIniziare,
            dipartimentiDetails = dipartimentiDetails
        )
    }
}