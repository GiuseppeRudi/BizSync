package com.bizsync.sync.orchestrator

import android.util.Log
import com.bizsync.backend.repository.TimbraturaRemoteRepositoryImpl
import com.bizsync.cache.dao.TimbraturaDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.domain.constants.enumClass.StatoTimbratura
import com.bizsync.domain.constants.enumClass.TipoTimbratura
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.model.Turno
import com.bizsync.domain.model.User
import com.bizsync.domain.repository.BadgeRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BadgeOrchestrator @Inject constructor(
    private val turnoDao: TurnoDao,
    private val timbraturaDao: TimbraturaDao,
    private val timbraturaRemoteRepositoryImpl: TimbraturaRemoteRepositoryImpl
) : BadgeRepository {

    override suspend fun getProssimoTurno(userId: String): Resource<ProssimoTurno> {
        return try {
            val now = LocalDateTime.now()
            val today = now.toLocalDate()

            val turniEntities = turnoDao.getTurniInRange(today, today.plusDays(7))

            // Filtra i turni del dipendente
            val turniDipendente = turniEntities
                .map { it.toDomain() }
                .filter { turno ->
                    val fineturno = LocalDateTime.of(turno.data, turno.orarioFine)
                    fineturno.isAfter(now.minusHours(2)) // Include turni fino a 2 ore dopo la fine
                }
                .sortedBy { LocalDateTime.of(it.data, it.orarioInizio) }

            if (turniDipendente.isEmpty()) {
                return Resource.Success(
                    ProssimoTurno(
                        turno = null,
                        tempoMancante = null,
                        abilitaTimbratura = false,
                        messaggioStato = "Nessun turno programmato"
                    )
                )
            }

            // Trova il turno più rilevante (in corso o prossimo)
            val turnoCorrente = findTurnoCorrente(turniDipendente, now)

            if (turnoCorrente == null) {
                return Resource.Success(
                    ProssimoTurno(
                        turno = null,
                        tempoMancante = null,
                        abilitaTimbratura = false,
                        messaggioStato = "Nessun turno disponibile"
                    )
                )
            }

            // Controlla le timbrature esistenti per questo turno
            val timbratureDomain = timbraturaRemoteRepositoryImpl.getByTurnoAndDipendente(turnoCorrente.id, userId)

            val haTimbratoEntrata = timbratureDomain.any { it.tipoTimbratura == TipoTimbratura.ENTRATA }
            val haTimbratoUscita = timbratureDomain.any { it.tipoTimbratura == TipoTimbratura.USCITA }

            // Determina il tipo di timbratura necessaria
            val tipoTimbraturaNecessaria = when {
                !haTimbratoEntrata -> TipoTimbratura.ENTRATA
                haTimbratoEntrata && !haTimbratoUscita -> TipoTimbratura.USCITA
                else -> TipoTimbratura.ENTRATA
            }

            // Calcola l'orario previsto e il tempo mancante
            val orarioPrevisto = when (tipoTimbraturaNecessaria) {
                TipoTimbratura.ENTRATA -> LocalDateTime.of(turnoCorrente.data, turnoCorrente.orarioInizio)
                TipoTimbratura.USCITA -> LocalDateTime.of(turnoCorrente.data, turnoCorrente.orarioFine)
            }

            val tempoMancante = Duration.between(now, orarioPrevisto)
            val minutiMancanti = tempoMancante.toMinutes()

            val abilitaTimbratura = when (tipoTimbraturaNecessaria) {
                TipoTimbratura.ENTRATA -> {
                    !haTimbratoEntrata && minutiMancanti >= -30 && minutiMancanti <= 30
                }
                TipoTimbratura.USCITA -> {
                    minutiMancanti >= -30 && minutiMancanti <= 120
                }
            }

            // Genera il messaggio di stato
            val messaggioStato = generateMessaggioStato(
                tipoTimbraturaNecessaria,
                minutiMancanti,
                haTimbratoEntrata,
                haTimbratoUscita,
                turnoCorrente
            )

            Resource.Success(
                ProssimoTurno(
                    turno = turnoCorrente,
                    tempoMancante = tempoMancante,
                    abilitaTimbratura = abilitaTimbratura,
                    messaggioStato = messaggioStato,
                    tipoTimbraturaNecessaria = tipoTimbraturaNecessaria,
                    haTimbratoEntrata = haTimbratoEntrata,
                    haTimbratoUscita = haTimbratoUscita,
                    orarioPrevisto = orarioPrevisto
                )
            )

        } catch (e: Exception) {
            Resource.Error("Errore nel recupero del prossimo turno: ${e.message}")
        }
    }

    private fun findTurnoCorrente(turni: List<Turno>, now: LocalDateTime): Turno? {
        // Prima cerca un turno in corso
        val turnoInCorso = turni.find { turno ->
            val inizio = LocalDateTime.of(turno.data, turno.orarioInizio)
            val fine = LocalDateTime.of(turno.data, turno.orarioFine)
            now.isAfter(inizio.minusMinutes(30)) && now.isBefore(fine.plusHours(2))
        }

        if (turnoInCorso != null) return turnoInCorso

        // Altrimenti prendi il prossimo turno
        return turni.find { turno ->
            val inizio = LocalDateTime.of(turno.data, turno.orarioInizio)
            inizio.isAfter(now.minusMinutes(30))
        }
    }

    private fun generateMessaggioStato(
        tipoTimbratura: TipoTimbratura,
        minutiMancanti: Long,
        haTimbratoEntrata: Boolean,
        haTimbratoUscita: Boolean,
        turno: Turno
    ): String {
        if (haTimbratoEntrata && haTimbratoUscita) {
            return "Turno completato ✓"
        }

        return when (tipoTimbratura) {
            TipoTimbratura.ENTRATA -> {
                when {
                    minutiMancanti < -30 -> "Turno iniziato - Timbra subito!"
                    minutiMancanti < 0 -> "È ora di timbrare l'entrata!"
                    minutiMancanti <= 10 -> "Timbratura entrata disponibile"
                    minutiMancanti <= 60 -> "Preparati per l'entrata"
                    else -> "Turno programmato"
                }
            }
            TipoTimbratura.USCITA -> {
                when {
                    minutiMancanti < -30 -> "Turno terminato - Timbra l'uscita!"
                    minutiMancanti < 0 -> "È ora di timbrare l'uscita!"
                    minutiMancanti <= 30 -> "Timbratura uscita disponibile a breve"
                    else -> "Turno in corso"
                }
            }
        }
    }
    override suspend fun creaTimbratura(
        turno: Turno,
        dipendente: User,
        azienda: Azienda,
        tipoTimbratura: TipoTimbratura,
        latitudine: Double?,
        longitudine: Double?
    ): Resource<Timbratura> {
        return try {
            Log.d("ORCHESTRATOR_DEBUG", "=== INIZIO creaTimbratura ===")
            Log.d("ORCHESTRATOR_DEBUG", "Parametri ricevuti:")
            Log.d("ORCHESTRATOR_DEBUG", "  turno.id: ${turno.id}")
            Log.d("ORCHESTRATOR_DEBUG", "  dipendente.uid: ${dipendente.uid}")
            Log.d("ORCHESTRATOR_DEBUG", "  azienda.idAzienda: ${azienda.idAzienda}")
            Log.d("ORCHESTRATOR_DEBUG", "  tipoTimbratura: $tipoTimbratura")
            Log.d("ORCHESTRATOR_DEBUG", "  latitudine: $latitudine")
            Log.d("ORCHESTRATOR_DEBUG", "  longitudine: $longitudine")

            val now = LocalDateTime.now()
            val zonaLavorativa = turno.getZonaLavorativaDipendente(dipendente.uid)

            Log.d("ORCHESTRATOR_DEBUG", "Zona lavorativa dipendente: $zonaLavorativa")
            Log.d("ORCHESTRATOR_DEBUG", "Coordinate azienda: lat=${azienda.latitudine}, lon=${azienda.longitudine}")
            Log.d("ORCHESTRATOR_DEBUG", "Tolleranza azienda: ${azienda.tolleranzaMetri} metri")

            // Calcola l'orario previsto in base al tipo di timbratura
            val orarioPrevisto = when (tipoTimbratura) {
                TipoTimbratura.ENTRATA -> LocalDateTime.of(turno.data, turno.orarioInizio)
                TipoTimbratura.USCITA -> LocalDateTime.of(turno.data, turno.orarioFine)
            }

            Log.d("ORCHESTRATOR_DEBUG", "Orario previsto: $orarioPrevisto")
            Log.d("ORCHESTRATOR_DEBUG", "Orario attuale: $now")

            // Verifica posizione se richiesta
            var posizioneVerificata = false
            var distanzaDallAzienda: Double? = null
            var dentroDellaTolleranza = true

            if (zonaLavorativa == ZonaLavorativa.IN_SEDE) {
                Log.d("ORCHESTRATOR_DEBUG", "Dipendente in sede - verifica posizione richiesta")

                if (latitudine != null && longitudine != null) {
                    Log.d("ORCHESTRATOR_DEBUG", "Coordinate presenti - calcolo distanza...")

                    distanzaDallAzienda = calcolaDistanza(
                        latitudine, longitudine,
                        azienda.latitudine, azienda.longitudine
                    )

                    Log.d("ORCHESTRATOR_DEBUG", "Distanza calcolata: ${distanzaDallAzienda}m")

                    dentroDellaTolleranza = distanzaDallAzienda <= azienda.tolleranzaMetri
                    posizioneVerificata = true

                    Log.d("ORCHESTRATOR_DEBUG", "Dentro tolleranza: $dentroDellaTolleranza")
                } else {
                    Log.e("ORCHESTRATOR_DEBUG", " ERRORE: Dipendente in sede ma coordinate sono null!")
                    Log.e("ORCHESTRATOR_DEBUG", "  latitudine: $latitudine")
                    Log.e("ORCHESTRATOR_DEBUG", "  longitudine: $longitudine")

                    // In questo caso dovremmo restituire un errore
                    return Resource.Error("Impossibile verificare la posizione: coordinate mancanti")
                }
            } else {
                Log.d("ORCHESTRATOR_DEBUG", "Dipendente NON in sede - verifica posizione non necessaria")
            }

            // Calcola stato timbratura
            val differenzaMinuti = Duration.between(orarioPrevisto, now).toMinutes().toInt()
            val statoTimbratura = when {
                differenzaMinuti < -5 -> StatoTimbratura.ANTICIPO
                differenzaMinuti <= 10 -> StatoTimbratura.IN_ORARIO
                differenzaMinuti <= 20 -> StatoTimbratura.RITARDO_LIEVE
                else -> StatoTimbratura.RITARDO_GRAVE
            }

            Log.d("ORCHESTRATOR_DEBUG", "Differenza minuti: $differenzaMinuti")
            Log.d("ORCHESTRATOR_DEBUG", "Stato timbratura: $statoTimbratura")



            Log.d("ORCHESTRATOR_DEBUG", "Coordinate finali da salvare:")


            val timbratura = Timbratura(
                idTurno = turno.id,
                idDipendente = dipendente.uid,
                idAzienda = azienda.idAzienda,
                tipoTimbratura = tipoTimbratura,
                dataOraTimbratura = now,
                dataOraPrevista = orarioPrevisto,
                zonaLavorativa = zonaLavorativa,
                posizioneVerificata = posizioneVerificata,
                distanzaDallAzienda = distanzaDallAzienda,
                dentroDellaTolleranza = dentroDellaTolleranza,
                statoTimbratura = statoTimbratura,
                minutiRitardo = if (differenzaMinuti > 0) differenzaMinuti else 0
            )

            Log.d("ORCHESTRATOR_DEBUG", "Oggetto Timbratura creato:")
            Log.d("ORCHESTRATOR_DEBUG", "  idTurno: ${timbratura.idTurno}")
            Log.d("ORCHESTRATOR_DEBUG", "  idDipendente: ${timbratura.idDipendente}")
            Log.d("ORCHESTRATOR_DEBUG", "  tipoTimbratura: ${timbratura.tipoTimbratura}")
            Log.d("ORCHESTRATOR_DEBUG", "  posizioneVerificata: ${timbratura.posizioneVerificata}")
            Log.d("ORCHESTRATOR_DEBUG", "  distanzaDallAzienda: ${timbratura.distanzaDallAzienda}")
            Log.d("ORCHESTRATOR_DEBUG", "  dentroDellaTolleranza: ${timbratura.dentroDellaTolleranza}")

            // Salva la timbratura
            Log.d("ORCHESTRATOR_DEBUG", "Salvataggio timbratura nel repository...")
            when (val result = timbraturaRemoteRepositoryImpl.addTimbratura(timbratura)) {
                is Resource.Success -> {
                    Log.d("ORCHESTRATOR_DEBUG", " Timbratura salvata con successo. ID Firebase: ${result.data}")
                    Resource.Success(timbratura.copy(idFirebase = result.data))
                }
                is Resource.Error -> {
                    Log.e("ORCHESTRATOR_DEBUG", " Errore nel salvataggio: ${result.message}")
                    result
                }
                else -> {
                    Log.e("ORCHESTRATOR_DEBUG", " Stato loading inaspettato dal repository")
                    Resource.Error("Errore durante il salvataggio")
                }
            }
        } catch (e: Exception) {
            Log.e("ORCHESTRATOR_DEBUG", " Eccezione in creaTimbratura: ${e.message}", e)
            Resource.Error("Errore nella creazione della timbratura: ${e.message}")
        }
    }

    private fun calcolaDistanza(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        Log.d("ORCHESTRATOR_DEBUG", "Calcolo distanza tra:")
        Log.d("ORCHESTRATOR_DEBUG", "  Punto 1 (dipendente): $lat1, $lon1")
        Log.d("ORCHESTRATOR_DEBUG", "  Punto 2 (azienda): $lat2, $lon2")

        val r = 6371000.0 // Raggio della Terra in metri
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanza = r * c

        Log.d("ORCHESTRATOR_DEBUG", "Distanza calcolata: ${distanza}m")
        return distanza
    }

    override suspend fun getTimbratureGiornaliere(idDipendente: String, data: LocalDate): Resource<List<Timbratura>> {
        return try {
            val timbrature = timbraturaDao.getUltimeTimbratureDipendente(idDipendente)
            Resource.Success(timbrature.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error("Errore nel recupero timbrature: ${e.message}")
        }
    }

    override suspend fun createBadgeVirtuale(user: User, azienda: Azienda): BadgeVirtuale {
        return BadgeVirtuale(
            idDipendente = user.uid,
            nome = user.nome,
            cognome = user.cognome,
            matricola = generateMatricola(user.uid),
            posizioneLavorativa = user.posizioneLavorativa,
            dipartimento = user.dipartimento,
            fotoUrl = user.photourl,
            idAzienda = azienda.idAzienda,
            nomeAzienda = azienda.nome,
            )
    }

    private fun generateMatricola(userId: String): String {
        return "EMP${userId.take(8).uppercase()}"
    }
}