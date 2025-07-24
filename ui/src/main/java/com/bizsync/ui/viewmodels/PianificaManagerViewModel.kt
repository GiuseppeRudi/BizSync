package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.TurnoOrchestrator
import com.bizsync.backend.repository.TurniAIRepository
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.TurnoDao
import com.bizsync.cache.mapper.toDomain
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.cache.mapper.toEntity
import com.bizsync.cache.mapper.toEntityList
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.constants.enumClass.TipoPausa
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.*
import com.bizsync.ui.model.ManagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PianificaManagerViewModel @Inject constructor(
    private val turnoOrchestrator: TurnoOrchestrator,
    private val turnoDao : TurnoDao,
    private val absenceDao: AbsenceDao,
    private val contrattiDao : ContrattoDao,
    var turniAIRepository: TurniAIRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PianificaManagerVM"
    }

    private val _uiState = MutableStateFlow(ManagerState())
    val uiState: StateFlow<ManagerState> = _uiState



    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(loading = loading) }
    }

    fun generateTurniWithAI(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        descrizioneAggiuntiva: String = "",
        idAzienda: String

    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingTurni = true) }

                val result = turniAIRepository.generateTurni(
                    dipartimento = dipartimento,
                    giornoSelezionato = giornoSelezionato,
                    dipendentiDisponibili = _uiState.value.disponibilitaMembriTurno,
                    statoSettimanale = _uiState.value.statoSettimanaleDipendenti,
                    turniEsistenti = _uiState.value.turniGiornalieriDip,
                    descrizioneAggiuntiva = descrizioneAggiuntiva
                )

                val turniConvertiti = result.turniGenerati.map { turnoAI ->
                    convertAITurnoToDomain(
                        turnoAI = turnoAI,
                        dipartimento = dipartimento.nomeArea,
                        giornoSelezionato = giornoSelezionato,
                        idAzienda =  idAzienda
                    )
                }


                _uiState.update {
                    it.copy(
                        isGeneratingTurni = false,
                        turniGeneratiAI = turniConvertiti,
                        showAIResultDialog = true,
                        hasChangeShift = true,
                        aiGenerationMessage = if (result.coperturaTotale) {
                            "Generati ${turniConvertiti.size} turni con copertura completa!"
                        } else {
                            "Generati ${turniConvertiti.size} turni. ${result.motivoCoperturaParziale}"
                        }
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Errore generazione turni AI: ${e.message}")
                _uiState.update {
                    it.copy(
                        isGeneratingTurni = false,
                        errorMessage = "Errore nella generazione automatica: ${e.message}"
                    )
                }
            }
        }
    }



    /**
     * Aggiorna la zona lavorativa per un singolo dipendente
     */
    fun aggiornaZonaLavorativaDipendente(idDipendente: String, zonaLavorativa: ZonaLavorativa) {
        val turnoCorrente = _uiState.value.turnoInModifica

        val zoneLavorativeAggiornate = turnoCorrente.zoneLavorative.toMutableMap()
        zoneLavorativeAggiornate[idDipendente] = zonaLavorativa

        val turnoAggiornato = turnoCorrente.copy(
            zoneLavorative = zoneLavorativeAggiornate,
            updatedAt = LocalDate.now()
        )

        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Zona lavorativa aggiornata per dipendente $idDipendente: $zonaLavorativa")
    }


    fun sincronizzaZoneLavorativeConDipendenti(dipendentiSelezionati: List<String>) {
        val turnoCorrente = _uiState.value.turnoInModifica

        val zoneLavorativeAttuali = turnoCorrente.zoneLavorative.toMutableMap()

        // Rimuovi zone per dipendenti non più selezionati
        val dipendentiRimossi = zoneLavorativeAttuali.keys - dipendentiSelezionati.toSet()
        dipendentiRimossi.forEach { zoneLavorativeAttuali.remove(it) }

        // Aggiungi zone di default per nuovi dipendenti
        dipendentiSelezionati.forEach { idDipendente ->
            if (!zoneLavorativeAttuali.containsKey(idDipendente)) {
                zoneLavorativeAttuali[idDipendente] = ZonaLavorativa.IN_SEDE // Default
            }
        }

        val turnoAggiornato = turnoCorrente.copy(
            zoneLavorative = zoneLavorativeAttuali,
            updatedAt = LocalDate.now()
        )

        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Zone lavorative sincronizzate con ${dipendentiSelezionati.size} dipendenti")
    }




    fun getZoneLavorativeAssegnate(): Map<String, ZonaLavorativa> {
        val turno = _uiState.value.turnoInModifica
        return turno.zoneLavorative
    }



    /**
     * Aggiorna la funzione aggiornaDipendenti esistente per sincronizzare le zone lavorative
     */
    fun aggiornaDipendentiConZone(nuoviDipendenti: List<String>) {
        val turnoCorrente = _uiState.value.turnoInModifica

        // Aggiorna i dipendenti
        val turnoAggiornato = turnoCorrente.copy(
            idDipendenti = nuoviDipendenti,
            updatedAt = LocalDate.now()
        )

        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }

        // Sincronizza le zone lavorative
        sincronizzaZoneLavorativeConDipendenti(nuoviDipendenti)

        Log.d(TAG, "Dipendenti e zone lavorative aggiornati: ${nuoviDipendenti.size} selezionati")
    }

    private fun convertAITurnoToDomain(
        turnoAI: TurnoGeneratoAI,
        dipartimento: String,
        giornoSelezionato: LocalDate,
        idAzienda: String
    ): Turno {
        val pause = turnoAI.pause.map { pausaAI ->
            Pausa(
                id = UUID.randomUUID().toString(),
                tipo = convertTipoPausa(pausaAI.tipo),
                durata = Duration.ofMinutes(pausaAI.durataMinuti.toLong()),
                èRetribuita = pausaAI.retribuita,
                note = null
            )
        }

        val note = turnoAI.note?.let {
            listOf(
                Nota(
                    id = UUID.randomUUID().toString(),
                    testo = it,
                    tipo = TipoNota.GENERALE,
                    autore = "AI",
                    createdAt = LocalDate.now()
                )
            )
        } ?: emptyList()

        return Turno(
            id = UUID.randomUUID().toString(),
            titolo = turnoAI.titolo,
            idAzienda = idAzienda,
            idDipendenti = turnoAI.idDipendenti,
            dipartimento = dipartimento,
            data = giornoSelezionato,
            orarioInizio = LocalTime.parse(turnoAI.orarioInizio),
            orarioFine = LocalTime.parse(turnoAI.orarioFine),
            pause = pause,
            note = note,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )
    }

    private fun convertTipoPausa(tipo: String): TipoPausa {
        return when (tipo) {
            "PAUSA_PRANZO" -> TipoPausa.PAUSA_PRANZO
            "PAUSA_CAFFE" -> TipoPausa.PAUSA_CAFFE
            "RIPOSO_BREVE" -> TipoPausa.RIPOSO_BREVE
            "TECNICA" -> TipoPausa.TECNICA
            "OBBLIGATORIA" -> TipoPausa.OBBLIGATORIA
            else -> TipoPausa.RIPOSO_BREVE
        }
    }

    fun confermaAITurni() {
        viewModelScope.launch {
            val turniAI = _uiState.value.turniGeneratiAI
            turnoDao.insertAll(turniAI.toEntityList())


            _uiState.update {
                it.copy(
                    turniGeneratiAI = emptyList(),
                    showAIResultDialog = false,
                    hasChangeShift = true,
                    successMessage = "Turni AI aggiunti con successo!"
                )
            }

        }
    }

    fun annullaAITurni() {
        _uiState.update {
            it.copy(
                turniGeneratiAI = emptyList(),
                showAIResultDialog = false
            )
        }
    }

    fun setturniDipartimento(giorno: DayOfWeek, idDipartimento: String) {
        val statoDipendentiSettiman = _uiState.value.dipendentiSettimana

        statoDipendentiSettiman[giorno]?.let { dipendentiGiorno ->
            // Filtra solo i dipendenti del dipartimento selezionato
            Log.d("TURNO_DEBUG", "✅ Dipendenti filtrati per $giorno e dipartimento $idDipartimento: $dipendentiGiorno")

            val utentiFiltrati = dipendentiGiorno.utenti.filter { it.dipartimento == idDipartimento }

            // Ricava solo gli stati relativi ai dipendenti filtrati
            val statiFiltrati = dipendentiGiorno.statoPerUtente
                .filterKeys { uid -> utentiFiltrati.any { it.uid == uid } }

            val nuovoDipendentiGiorno = DipendentiGiorno(
                utenti = utentiFiltrati,
                statoPerUtente = statiFiltrati
            )

            _uiState.update {
                it.copy(disponibilitaMembriTurno = nuovoDipendentiGiorno)
            }

            Log.d("TURNO_DEBUG", "✅ DAIIIII per $giorno e dipartimento $idDipartimento: ${_uiState.value.disponibilitaMembriTurno }}")

        }
    }


    fun inizializzaDatiDipendenti(dipendenti : List<User>)
    {
        _uiState.update { it.copy(dipendenti = dipendenti) }
    }

    fun inizializzaDatiWeeklyRiferimento( dipendenti : List<User>) {
        viewModelScope.launch {
            try {

                // LE ASSENZE MI SERVONO SOLO DELLA SETTIMANA DI RIFERIMENTO
                val assDeferred = async { absenceDao.getAbsences().toDomainList() }
                val contrDeferred = async { contrattiDao.getContratti().toDomainList() }

                val assenze = assDeferred.await()
                val contratti = contrDeferred.await()

                _uiState.update {
                    it.copy(
                        dipendenti = dipendenti,
                        assenze = assenze,
                        contratti = contratti
                    )
                }

                setDipendentiStato()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore inizializzazione (async): ${e.message}")
            }
        }
    }
    fun setDipendentiStato()
    {
        val listaDipendenti = _uiState.value.dipendenti
        val assenzeFiltrate = _uiState.value.assenze
        val contratti = _uiState.value.contratti

        val dipendentiSettimana = calcolaDipendentiSettimana(
            dipendenti = listaDipendenti,
            assenzeSettimana = assenzeFiltrate
        )
        Log.d(TAG, "caricamento dipendenti settimana: $dipendentiSettimana")
        _uiState.update { it.copy(dipendentiSettimana = dipendentiSettimana) }

        _uiState.update { it.copy( statoSettimanaleDipendenti = calcolaStatoSettimanaleDipendenti(listaDipendenti, contratti)) }

        Log.d(TAG, "caricamento dipendenti stato settimanale: ${_uiState.value.statoSettimanaleDipendenti}")

    }

    fun calcolaStatoSettimanaleDipendenti(
        dipendenti: List<User>,
        contratti: List<Contratto>
    ): Map<String, StatoSettimanaleDipendente> {
        return dipendenti.associate { user ->
            val contratto = contratti.find { it.idDipendente == user.uid }
            val maxOre  : Int =  toIntSafe(contratto?.oreSettimanali  )?: 0
            user.uid to StatoSettimanaleDipendente(
                oreContrattoSettimana = maxOre
            )
        }
    }

    fun toIntSafe(input: String?): Int? {
        return input?.toIntOrNull()
    }




    fun calcolaDipendentiSettimana(
        dipendenti: List<User>,
        assenzeSettimana: List<Absence>
    ): Map<DayOfWeek, DipendentiGiorno> {

        val result = mutableMapOf<DayOfWeek, DipendentiGiorno>()

        // Per ogni giorno della settimana (LUN → DOM)
        for (giorno in DayOfWeek.entries) {

            // Stato per ogni dipendente in quel giorno
            val statoPerUtente = mutableMapOf<String, StatoDipendente>()

            for (dip in dipendenti) {
                val assenzeUtente = assenzeSettimana.filter { it.idUser == dip.uid }

                var isTotale = false
                var parziale: AssenzaParziale? = null

                for (assenza in assenzeUtente) {
                    if (assenza.status != AbsenceStatus.APPROVED) continue

                    // Se l'assenza include questo giorno
                    if (!giornoInAssenza(giorno, assenza)) continue

                    when {
                        assenza.totalDays != null -> {
                            isTotale = true
                            break
                        }
                        assenza.totalHours != null && assenza.startTime != null && assenza.endTime != null -> {
                            parziale = AssenzaParziale(
                                inizio = assenza.startTime!!,
                                fine = assenza.endTime!!
                            )
                        }
                    }
                }

                statoPerUtente[dip.uid] = StatoDipendente(
                    isAssenteTotale = isTotale,
                    assenzaParziale = if (!isTotale) parziale else null
                )
            }

            result[giorno] = DipendentiGiorno(
                utenti = dipendenti,
                statoPerUtente = statoPerUtente
            )
        }

        return result
    }

    fun giornoInAssenza(day: DayOfWeek, absence: Absence): Boolean {
        var current = absence.startDate
        val end = absence.endDate

        while (!current.isAfter(end)) {
            if (current.dayOfWeek == day) {
                return true
            }
            current = current.plusDays(1)
        }

        return false
    }


    suspend fun setTurniSettimanaliSuspend(startWeek: LocalDate) {
        when (val result = turnoOrchestrator.fetchTurniSettimana(startWeek)) {
            is Resource.Success -> {
                val turni = result.data
                val grouped = turni.groupBy { it.data.dayOfWeek }
                val allDays = DayOfWeek.entries.associateWith { grouped[it] ?: emptyList() }
                _uiState.update { current -> current.copy(turniSettimanali = allDays) }
            }
            else -> {
                val empty = DayOfWeek.entries.associateWith { emptyList<Turno>() }
                _uiState.update { current -> current.copy(turniSettimanali = empty) }
            }
        }
    }

    fun setTurniGiornalieriDipartimento(dipartimentoId: String) {
        val turniMap = _uiState.value.turniSettimanali
        val giornalieri = turniMap.values.flatten()
            .filter { it.dipartimento == dipartimentoId }
        _uiState.update {
            it.copy(turniGiornalieriDip = giornalieri)
        }
    }


    fun setTurniSettimanali(startWeek: LocalDate, idAzienda: String) {
        viewModelScope.launch {

            when (val result = turnoOrchestrator.fetchTurniSettimana(startWeek,idAzienda)) {
                is Resource.Success -> {
                    val turni = result.data

                    val grouped = turni.groupBy { it.data.dayOfWeek }
                    val allDays = DayOfWeek.entries.associateWith { grouped[it] ?: emptyList() }


                    _uiState.update { current -> current.copy(turniSettimanali = allDays) }
                }
                is Resource.Error -> {
                    val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }
                    _uiState.update { current -> current.copy(turniSettimanali = allDays) }
                }
                is Resource.Empty -> {
                    val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }
                    _uiState.update { current -> current.copy(turniSettimanali = allDays) }
                }
            }
        }
    }




    fun setTurniGiornalieri(dayOfWeek: DayOfWeek, dipartimentiDelGiorno: List<AreaLavoro>) {
        viewModelScope.launch {
            val currentTurniSettimanali = _uiState.value.turniSettimanali
            val turniDelGiorno = currentTurniSettimanali[dayOfWeek] ?: emptyList()

            val turniPerDipartimento: Map<String, List<Turno>> = dipartimentiDelGiorno.associate { dipartimento ->
                dipartimento.nomeArea to turniDelGiorno.filter { it.dipartimento == dipartimento.nomeArea }
            }


            _uiState.update { current ->
                current.copy(turniGiornalieri = turniPerDipartimento, loading = false)
            }
        }
    }


    fun caricaTurniSettimanaEDipartimento(weekStart: LocalDate, dipartimento: String) {
        viewModelScope.launch {
            try {
                setLoading(true)

                setTurniSettimanaliSuspend(weekStart)

                setTurniGiornalieriDipartimento(dipartimento)

                _uiState.update { it.copy(hasChangeShift = false) }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore durante il caricamento turni: ${e.message}")
            } finally {
                setLoading(false)
            }
        }
    }


    fun pulisciTurnoInModifica() {
        _uiState.update {
            it.copy(
                turnoInModifica = Turno(),
                showDialogCreateShift = false
            )
        }
    }


    /**
     * Chiude il dialog per gestire le pause
     */
    fun chiudiGestionePause() {
        _uiState.update {
            it.copy(
                showPauseDialog = false,
                showAddEditPauseDialog = false,
                pausaInModifica = null
            )
        }
        Log.d(TAG, "Chiuso dialog gestione pause")
    }

    /**
     * Inizializza una nuova pausa per la modifica
     */
    fun iniziaNuovaPausa() {
        val nuovaPausa = Pausa(
            id = "",
            durata = Duration.ofMinutes(30),
            tipo = TipoPausa.PAUSA_PRANZO,
            èRetribuita = false,
            note = null
        )

        _uiState.update {
            it.copy(
                pausaInModifica = nuovaPausa,
                showAddEditPauseDialog = true
            )
        }
        Log.d(TAG, "Iniziata nuova pausa")
    }


    fun caricaPausaPerModifica(pausa: Pausa) {
        _uiState.update {
            it.copy(
                pausaInModifica = pausa,
                showAddEditPauseDialog = true
            )
        }
        Log.d(TAG, "Caricata pausa per modifica: ${pausa.tipo}")
    }

    /**
     * Pulisce la pausa in modifica
     */
    fun pulisciPausaInModifica() {
        _uiState.update {
            it.copy(
                pausaInModifica = null,
                showAddEditPauseDialog = false
            )
        }
        Log.d(TAG, "Pulita pausa in modifica")
    }


    /**
     * Aggiorna il tipo della pausa in modifica
     */
    fun aggiornaTipoPausa(nuovoTipo: TipoPausa) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(tipo = nuovoTipo)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Tipo pausa aggiornato: $nuovoTipo")
    }

    /**
     * Aggiorna la durata della pausa in modifica
     */
    fun aggiornaDurataPausa(nuovaDurata: Duration) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(durata = nuovaDurata)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Durata pausa aggiornata: ${nuovaDurata.toMinutes()} minuti")
    }

    /**
     * Aggiorna se la pausa è retribuita
     */
    fun aggiornaRetribuitaPausa(isRetribuita: Boolean) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(èRetribuita = isRetribuita)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Retribuita pausa aggiornata: $isRetribuita")
    }

    /**
     * Aggiorna le note della pausa in modifica
     */
    fun aggiornaNotePausa(nuoveNote: String?) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(note = nuoveNote?.takeIf { it.isNotBlank() })
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Note pausa aggiornate")
    }


    /**
     * Salva la pausa in modifica nella lista delle pause del turno
     */
    fun salvaPausaInTurno() {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val turnoCorrente = _uiState.value.turnoInModifica

        val pauseAttuali = turnoCorrente.pause.toMutableList()
        val isNuovaPausa = pausaCorrente.id.isEmpty()

        val pausaFinale = if (isNuovaPausa) {
            pausaCorrente.copy(id = UUID.randomUUID().toString())
        } else {
            pausaCorrente
        }

        if (isNuovaPausa) {
            // Aggiungi nuova pausa
            pauseAttuali.add(pausaFinale)
            Log.d(TAG, "Aggiunta nuova pausa: ${pausaFinale.tipo}")
        } else {
            // Modifica pausa esistente
            val index = pauseAttuali.indexOfFirst { it.id == pausaFinale.id }
            if (index != -1) {
                pauseAttuali[index] = pausaFinale
                Log.d(TAG, "Modificata pausa esistente: ${pausaFinale.tipo}")
            }
        }

        val turnoAggiornato = turnoCorrente.copy(
            pause = pauseAttuali,
            updatedAt = LocalDate.now()
        )

        _uiState.update {
            it.copy(
                turnoInModifica = turnoAggiornato,
                pausaInModifica = null,
                showAddEditPauseDialog = false
            )
        }

        Log.d(TAG, "Pausa salvata nel turno. Totale pause: ${pauseAttuali.size}")
    }

    /**
     * Elimina una pausa dalla lista del turno
     */
    fun eliminaPausaDalTurno(pausaId: String) {
        val turnoCorrente = _uiState.value.turnoInModifica

        val pauseAggiornate = turnoCorrente.pause.filter { it.id != pausaId }
        val turnoAggiornato = turnoCorrente.copy(
            pause = pauseAggiornate,
            updatedAt = LocalDate.now()
        )

        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }

        Log.d(TAG, "Pausa eliminata dal turno. Pause rimanenti: ${pauseAggiornate.size}")
    }


    /**
     * Valida la pausa corrente
     */
    fun validaPausa(): ValidationResult {
        val pausa = _uiState.value.pausaInModifica
            ?: return ValidationResult.Error("Nessuna pausa in modifica")

        val errori = mutableListOf<String>()

        // Validazione durata
        if (pausa.durata.toMinutes() <= 0) {
            errori.add("La durata deve essere maggiore di 0 minuti")
        } else if (pausa.durata.toMinutes() > 480) {
            errori.add("La durata non può superare 8 ore (480 minuti)")
        }

        val noteVal = pausa.note
        if (noteVal != null && noteVal.length > 200) {
            errori.add("Le note non possono superare i 200 caratteri")
        }


        return if (errori.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errori.joinToString("\n"))
        }
    }

    fun setShowPauseDialog(show: Boolean)
    {
        _uiState.update { it.copy(showPauseDialog = show) }
    }



    fun aggiornaTitolo(nuovoTitolo: String) {
        val turnoCorrente = _uiState.value.turnoInModifica
        val turnoAggiornato = turnoCorrente.copy(
            titolo = nuovoTitolo,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Titolo aggiornato: $nuovoTitolo")
    }

    fun aggiornaOrarioInizio(nuovoOrario: LocalTime) {
        val turnoCorrente = _uiState.value.turnoInModifica
        val turnoAggiornato = turnoCorrente.copy(
            orarioInizio = nuovoOrario,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Orario inizio aggiornato: $nuovoOrario")
    }

    fun aggiornaOrarioFine(nuovoOrario: LocalTime) {
        val turnoCorrente = _uiState.value.turnoInModifica
        val turnoAggiornato = turnoCorrente.copy(
            orarioFine = nuovoOrario,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Orario fine aggiornato: $nuovoOrario")
    }



    fun aggiornaNote(nuoveNote: List<Nota>) {
        val turnoCorrente = _uiState.value.turnoInModifica
        val turnoAggiornato = turnoCorrente.copy(
            note = nuoveNote,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Note aggiornate: ${nuoveNote.size} note")
    }



    /**
     * Carica un turno esistente per la modifica
     */
    fun editTurno(turnoId: String) {
        viewModelScope.launch {
            try {
                setLoading(true)

                // Recupera il turno dal database locale
                val turnoEntity = turnoDao.getTurnoById(turnoId)

                if (turnoEntity != null && !turnoEntity.isDeleted) {
                    // Converti in domain model e caricalo per la modifica
                    val turno = turnoEntity.toDomain()

                    _uiState.update {
                        it.copy(
                            turnoInModifica = turno,
                            showDialogCreateShift = true // Apre la schermata di modifica
                        )
                    }

                    Log.d(TAG, "✅ Turno caricato per modifica: ${turno.titolo}")

                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Turno non trovato o già eliminato"
                        )
                    }
                    Log.e(TAG, "❌ Turno non trovato per modifica: $turnoId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore durante il caricamento turno per modifica: ${e.message}")
                _uiState.update {
                    it.copy(
                        errorMessage = "Errore durante il caricamento del turno: ${e.message}"
                    )
                }
            } finally {
                setLoading(false)
            }
        }
    }


    fun deleteTurnoWithConfirmation(turnoId: String, onConfirm: () -> Unit) {
        viewModelScope.launch {
            try {
                val turnoEntity = turnoDao.getTurnoById(turnoId)

                if (turnoEntity != null && !turnoEntity.isDeleted) {
                    // Mostra dialog di conferma
                    _uiState.update {
                        it.copy(
                            turnoToDelete = turnoEntity.toDomain(),
                            showDeleteConfirmDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore durante la preparazione eliminazione: ${e.message}")
            }
        }
    }

    /**
     * Annulla l'eliminazione del turno
     */
    fun cancelDeleteTurno() {
        _uiState.update {
            it.copy(
                turnoToDelete = null,
                showDeleteConfirmDialog = false
            )
        }
    }

    /**
     * Elimina un turno (soft delete - imposta isDeleted = true)
     */
    fun deleteTurno(turnoId: String) {
        viewModelScope.launch {
            try {
                setLoading(true)

                // Recupera il turno dal database
                val turnoEntity = turnoDao.getTurnoById(turnoId)

                if (turnoEntity != null && !turnoEntity.isDeleted) {
                    // Effettua soft delete impostando isDeleted = true
                    val turnoEliminato = turnoEntity.copy(
                        isDeleted = true,
                        isSynced = false, // Marca come non sincronizzato per propagare la modifica
                        updatedAt = com.google.firebase.Timestamp.now()
                    )

                    turnoDao.update(turnoEliminato)

                    Log.d(TAG, "✅ Turno eliminato (soft delete): ${turnoEntity.titolo}")

                    _uiState.update {
                        it.copy(
                            hasChangeShift = true,
                            successMessage = "Turno eliminato con successo"
                        )
                    }

                } else {
                    _uiState.update {
                        it.copy(
                            errorMessage = if (turnoEntity?.isDeleted == true) {
                                "Il turno è già stato eliminato"
                            } else {
                                "Turno non trovato"
                            }
                        )
                    }
                    Log.e(TAG, "❌ Turno non trovato o già eliminato: $turnoId")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore durante l'eliminazione turno: ${e.message}")
                _uiState.update {
                    it.copy(
                        errorMessage = "Errore durante l'eliminazione: ${e.message}"
                    )
                }
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Conferma l'eliminazione del turno
     */
    fun confirmDeleteTurno() {
        val turnoToDelete = _uiState.value.turnoToDelete
        if (turnoToDelete != null) {
            deleteTurno(turnoToDelete.id)
            _uiState.update {
                it.copy(
                    turnoToDelete = null,
                    showDeleteConfirmDialog = false
                )
            }
        }
    }

    fun applicaTurnoFrequente(turno: TurnoFrequente) {
        aggiornaTitolo(turno.nome)

        runCatching {
            val inizio = LocalTime.parse(turno.oraInizio)
            val fine = LocalTime.parse(turno.oraFine)

            aggiornaOrarioInizio(inizio)
            aggiornaOrarioFine(fine)

            Log.d("TurnoFrequente", "✅ Applicato turno frequente: ${turno.nome}")
        }.onFailure {
            Log.e("TurnoFrequente", "❌ Errore parsing orari: ${it.message}")
        }
    }

    /**
     * Salva il turno corrente
     */
    fun saveTurno(dipartimento : String, giornoSelezionato : LocalDate, idAzienda: String) {
        viewModelScope.launch {

            _uiState.update { it.copy(loading = true) }


            val turno = _uiState.value.turnoInModifica
            Log.d("SAVE_TURNO", "💾 Salvataggio turno in Room: ${turno.id}")

            val isNewTurno = !turnoDao.exists(turno.id)

            Log.d("SAVE_TURNO", " è NUOVO : $isNewTurno")


            val turnoToSave = if (isNewTurno) {
                turno.copy(
                    dipartimento = dipartimento,
                    data = giornoSelezionato,
                    idAzienda = idAzienda
                )
            } else
                turno


            val turnoEntity = turnoToSave.toEntity()



            try {
                if (isNewTurno) {
                    turnoDao.insert(turnoEntity) // Inserisci nuovo
                } else {
                    val turnoDaAggiornare = turnoEntity

                    val turnoDaSync = turnoDaAggiornare.copy(isSynced = false)
                    turnoDao.update(turnoDaSync)
                }

                Log.d(TAG, "✅ Turno salvato localmente in Room")

                _uiState.update { it.copy(hasChangeShift = true, loading = false, turnoInModifica = Turno()) }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore salvataggio turno locale: ${e.message}")
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = "Errore durante il salvataggio locale: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }


    /**
     * Ottiene i dipendenti selezionati per il turno corrente
     */
    fun getDipendentiSelezionati(): List<User> {
        val turno = _uiState.value.turnoInModifica
        val tuttiDipendenti = _uiState.value.dipendenti

        return tuttiDipendenti.filter { it.uid in turno.idDipendenti }
    }


    /**
     * Calcola la durata totale del turno corrente
     */
    fun calcolaDurataTurnoCorrente(): String {
        val turno = _uiState.value.turnoInModifica

        val durataLavoro = turno.calcolaDurata()
        val durataPause = turno.pause.sumOf { it.durata.toMinutes() }
        val durataEffettiva = durataLavoro * 60 - durataPause

        val ore = durataEffettiva / 60
        val minuti = durataEffettiva % 60

        return "${ore}h ${minuti}m"
    }
}


sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}


