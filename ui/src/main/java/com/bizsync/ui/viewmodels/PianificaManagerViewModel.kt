package com.bizsync.ui.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.TurnoOrchestrator
import com.bizsync.cache.dao.AbsenceDao
import com.bizsync.cache.dao.ContrattoDao
import com.bizsync.cache.dao.UserDao
import com.bizsync.cache.mapper.toDomainList
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.TipoPausa
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
import javax.inject.Inject

@HiltViewModel
class PianificaManagerViewModel @Inject constructor(
    private val turnoOrchestrator: TurnoOrchestrator,
    private val userDao: UserDao,
    private val absenceDao: AbsenceDao,
    private val contrattiDao : ContrattoDao
) : ViewModel() {

    companion object {
        private const val TAG = "PianificaManagerVM"
    }

    private val _uiState = MutableStateFlow(ManagerState())
    val uiState: StateFlow<ManagerState> = _uiState

    // ========== GESTIONE LOADING E STATO GENERALE ==========

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(loading = loading) }
    }


    fun inizializzaDatiDipendenti(idAzienda: String) {
        viewModelScope.launch {
            try {
                val dipDeferred = async { userDao.getDipendenti(idAzienda).toDomainList() }
                val assDeferred = async { absenceDao.getAbsencesByAzienda(idAzienda).toDomainList() }
                val contrDeferred = async { contrattiDao.getContratti(idAzienda).toDomainList() }

                val dipendenti = dipDeferred.await()
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


    fun setDipendenti(idAzienda: String) {
        viewModelScope.launch {
            try {
                val userentity = userDao.getDipendenti(idAzienda)
                val userDomain = userentity.toDomainList()
                _uiState.update { it.copy(dipendenti = userDomain) }
                Log.d(TAG, " caricamento dipendenti: ${userDomain}")
            } catch (e: Exception) {
                Log.e(TAG, "Errore caricamento dipendenti: ${e.message}")
            }
        }
    }

    fun setAssenze(idAzienda: String) {
        viewModelScope.launch {
            try {
                val absenceEntity = absenceDao.getAbsencesByAzienda(idAzienda, )
                val absenceDomain = absenceEntity.toDomainList()
                _uiState.update { it.copy(assenze = absenceDomain) }
                Log.d(TAG, " caricamento assenze: ${absenceDomain}")

            } catch (e: Exception) {
                Log.e(TAG, "Errore caricamento assenze: ${e.message}")
            }
        }
    }

    fun setContratti(idAzienda: String) {
        viewModelScope.launch {
            try {
                val contrattiEntity = contrattiDao.getContratti(idAzienda, )
                val contrattiDomain = contrattiEntity.toDomainList()
                _uiState.update { it.copy(contratti = contrattiDomain) }
                Log.d(TAG, " caricamento contratti: ${contrattiDomain}")

            } catch (e: Exception) {
                Log.e(TAG, "Errore caricamento contratti: ${e.message}")
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
        Log.d(TAG, "caricamento dipendenti settimana: ${dipendentiSettimana}")
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

        // Inizializza struttura finale
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
                            break // non serve guardare altro
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

    fun setTurniSettimanali(startWeek: LocalDate) {
        viewModelScope.launch {
            when (val result = turnoOrchestrator.fetchTurniSettimana(startWeek)) {
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
                dipartimento.id to turniDelGiorno.filter { it.dipartimentoId == dipartimento.id }
            }


            _uiState.update { current ->
                current.copy(turniGiornalieri = turniPerDipartimento, loading = false)
            }
        }
    }

    fun setTurniGiornalieriDipartimento(dipartimentoId: String) {
        _uiState.update {
            it.copy(turniGiornalieriDip = _uiState.value.turniGiornalieri[dipartimentoId] ?: emptyList())
        }
    }

    fun setShowDialogCreateShift(show: Boolean) {
        _uiState.update { it.copy(showDialogCreateShift = show) }
    }

    // ========== GESTIONE TURNO IN MODIFICA ==========

    /**
     * Inizializza un nuovo turno per la modifica
     */
    fun iniziaNuovoTurno(
        giornoSelezionato: LocalDate,
        dipartimentoId: String = "",
        idAzienda: String
    ) {
        val nuovoTurno = Turno(
            id = "",
            titolo = "",
            idAzienda = idAzienda,
            data = giornoSelezionato,
            orarioInizio = LocalTime.of(8, 0),
            orarioFine = LocalTime.of(17, 0),
            idDipendenti = emptyList(),
            dipartimentoId = dipartimentoId,
            note = emptyList(),
            pause = emptyList(),
            isConfermato = false,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )

        _uiState.update {
            it.copy(
                turnoInModifica = nuovoTurno,
                showDialogCreateShift = true
            )
        }

        Log.d(TAG, "Iniziato nuovo turno per giorno: $giornoSelezionato")
    }

    /**
     * Carica un turno esistente per la modifica
     */
    fun caricaTurnoPerModifica(turno: Turno) {
        _uiState.update {
            it.copy(
                turnoInModifica = turno,
                showDialogCreateShift = true
            )
        }

        Log.d(TAG, "Caricato turno per modifica: ${turno.titolo}")
    }

    /**
     * Pulisce il turno in modifica
     */
    fun pulisciTurnoInModifica() {
        _uiState.update {
            it.copy(
                turnoInModifica = Turno(),
                showDialogCreateShift = false
            )
        }
    }


    // ========== GESTIONE PAUSE ==========

    /**
     * Apre il dialog per gestire le pause
     */
    fun apriGestionePause() {
        _uiState.update { it.copy(showPauseDialog = true) }
        Log.d(TAG, "Aperto dialog gestione pause")
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

    /**
     * Carica una pausa esistente per la modifica
     */
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

// ========== AGGIORNAMENTO CAMPI PAUSA ==========

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
    fun aggiornaRetribuitaPausa(èRetribuita: Boolean) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(èRetribuita = èRetribuita)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Retribuita pausa aggiornata: $èRetribuita")
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

// ========== GESTIONE LISTA PAUSE DEL TURNO ==========

    /**
     * Salva la pausa in modifica nella lista delle pause del turno
     */
    fun salvaPausaInTurno() {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val turnoCorrente = _uiState.value.turnoInModifica ?: return

        val pauseAttuali = turnoCorrente.pause.toMutableList()
        val isNuovaPausa = pausaCorrente.id.isEmpty()

        val pausaFinale = if (isNuovaPausa) {
            pausaCorrente.copy(id = java.util.UUID.randomUUID().toString())
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
        val turnoCorrente = _uiState.value.turnoInModifica ?: return

        val pauseAggiornate = turnoCorrente.pause.filter { it.id != pausaId }
        val turnoAggiornato = turnoCorrente.copy(
            pause = pauseAggiornate,
            updatedAt = LocalDate.now()
        )

        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }

        Log.d(TAG, "Pausa eliminata dal turno. Pause rimanenti: ${pauseAggiornate.size}")
    }

// ========== VALIDAZIONE PAUSA ==========

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

// ========== UTILITY PAUSE ==========

    /**
     * Calcola la durata totale delle pause del turno corrente
     */
    fun calcolaDurataTotalePause(): Long {
        val turno = _uiState.value.turnoInModifica ?: return 0L
        return turno.pause.sumOf { it.durata.toMinutes() }
    }

    /**
     * Verifica se esistono pause retribuite nel turno corrente
     */
    fun haPauseRetribuite(): Boolean {
        val turno = _uiState.value.turnoInModifica ?: return false
        return turno.pause.any { it.èRetribuita }
    }

    /**
     * Ottiene le pause raggruppate per tipo
     */
    fun getPausePerTipo(): Map<TipoPausa, List<Pausa>> {
        val turno = _uiState.value.turnoInModifica ?: return emptyMap()
        return turno.pause.groupBy { it.tipo }
    }
    // ========== AGGIORNAMENTO CAMPI TURNO ==========

    fun aggiornaTitolo(nuovoTitolo: String) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            titolo = nuovoTitolo,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Titolo aggiornato: $nuovoTitolo")
    }

    fun aggiornaOrarioInizio(nuovoOrario: LocalTime) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            orarioInizio = nuovoOrario,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Orario inizio aggiornato: $nuovoOrario")
    }

    fun aggiornaOrarioFine(nuovoOrario: LocalTime) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            orarioFine = nuovoOrario,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Orario fine aggiornato: $nuovoOrario")
    }

    fun aggiornaDipartimento(nuovoDipartimentoId: String) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            dipartimentoId = nuovoDipartimentoId,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Dipartimento aggiornato: $nuovoDipartimentoId")
    }

    fun aggiornaDipendenti(nuoviDipendenti: List<String>) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            idDipendenti = nuoviDipendenti,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Dipendenti aggiornati: ${nuoviDipendenti.size} selezionati")
    }

    fun aggiornaPause(nuovePause: List<Pausa>) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            pause = nuovePause,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Pause aggiornate: ${nuovePause.size} pause")
    }

    fun aggiornaNote(nuoveNote: List<Nota>) {
        val turnoCorrente = _uiState.value.turnoInModifica ?: return
        val turnoAggiornato = turnoCorrente.copy(
            note = nuoveNote,
            updatedAt = LocalDate.now()
        )
        _uiState.update { it.copy(turnoInModifica = turnoAggiornato) }
        Log.d(TAG, "Note aggiornate: ${nuoveNote.size} note")
    }

    // ========== VALIDAZIONE TURNO ==========

    /**
     * Valida il turno corrente
     */
    fun validaTurno(): ValidationResult {
        val turno = _uiState.value.turnoInModifica
            ?: return ValidationResult.Error("Nessun turno in modifica")

        val errori = mutableListOf<String>()

        // Validazione titolo
        if (turno.titolo.isBlank()) {
            errori.add("Il titolo è obbligatorio")
        } else if (turno.titolo.length > 50) {
            errori.add("Il titolo non può superare i 50 caratteri")
        }

        // Validazione orari
        if (turno.orarioInizio >= turno.orarioFine) {
            errori.add("L'orario di inizio deve essere precedente all'orario di fine")
        }

        // Validazione dipartimento
        if (turno.dipartimentoId.isBlank()) {
            errori.add("Seleziona un dipartimento")
        }

        // Validazione dipendenti
        if (turno.idDipendenti.isEmpty()) {
            errori.add("Seleziona almeno un dipendente")
        }

        return if (errori.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errori.joinToString("\n"))
        }
    }

    // ========== SALVATAGGIO TURNO ==========

    /**
     * Salva il turno corrente
     */
    fun salvaTurno() {
//        viewModelScope.launch {
//            val turno = _uiState.value.turnoInModifica
//            if (turno == null) {
//                Log.e(TAG, "Nessun turno da salvare")
//                return@launch
//            }
//
//            // Validazione
//            when (val validationResult = validaTurno()) {
//                is ValidationResult.Error -> {
//                    Log.e(TAG, "Errore validazione: ${validationResult.message}")
//                    _uiState.update { it.copy(errorMessage = validationResult.message) }
//                    return@launch
//                }
//                ValidationResult.Success -> {
//                    // Continua con il salvataggio
//                }
//            }
//
//            setLoading(true)
//
//            try {
//                val isNuovoTurno = turno.id.isEmpty()
//
//                val result = if (isNuovoTurno) {
//                    turnoOrchestrator.creaTurno(turno)
//                } else {
//                    turnoOrchestrator.aggiornaTurno(turno)
//                }
//
//                when (result) {
//                    is Resource.Success -> {
//                        Log.d(TAG, "Turno salvato con successo: ${turno.titolo}")
//
//                        // Aggiorna la lista dei turni
//                        setTurniSettimanali(turno.data)
//
//                        // Pulisci il turno in modifica
//                        pulisciTurnoInModifica()
//
//                        _uiState.update { it.copy(successMessage = "Turno salvato con successo") }
//                    }
//                    is Resource.Error -> {
//                        Log.e(TAG, "Errore salvataggio turno: ${result.message}")
//                        _uiState.update { it.copy(errorMessage = result.message) }
//                    }
//                    else -> {
//                        Log.e(TAG, "Errore imprevisto durante il salvataggio")
//                        _uiState.update { it.copy(errorMessage = "Errore imprevisto") }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Eccezione durante il salvataggio: ${e.message}")
//                _uiState.update { it.copy(errorMessage = "Errore durante il salvataggio: ${e.message}") }
//            } finally {
//                setLoading(false)
//            }
//        }
    }

    /**
     * Elimina un turno
     */
    fun eliminaTurno(turnoId: String) {
//        viewModelScope.launch {
//            setLoading(true)
//
//            try {
//                when (val result = turnoOrchestrator.eliminaTurno(turnoId)) {
//                    is Resource.Success -> {
//                        Log.d(TAG, "Turno eliminato con successo: $turnoId")
//
//                        // Aggiorna la lista dei turni
//                        val turnoCorrente = _uiState.value.turnoInModifica
//                        if (turnoCorrente != null) {
//                            setTurniSettimanali(turnoCorrente.data)
//                        }
//
//                        _uiState.update { it.copy(successMessage = "Turno eliminato con successo") }
//                    }
//                    is Resource.Error -> {
//                        Log.e(TAG, "Errore eliminazione turno: ${result.message}")
//                        _uiState.update { it.copy(errorMessage = result.message) }
//                    }
//                    else -> {
//                        _uiState.update { it.copy(errorMessage = "Errore imprevisto durante l'eliminazione") }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Eccezione durante l'eliminazione: ${e.message}")
//                _uiState.update { it.copy(errorMessage = "Errore durante l'eliminazione: ${e.message}") }
//            } finally {
//                setLoading(false)
//            }
//        }
    }

    // ========== GESTIONE MESSAGGI ==========

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // ========== UTILITY ==========

    /**
     * Ottiene i dipendenti selezionati per il turno corrente
     */
    fun getDipendentiSelezionati(): List<User> {
        val turno = _uiState.value.turnoInModifica ?: return emptyList()
        val tuttiDipendenti = _uiState.value.dipendenti

        return tuttiDipendenti.filter { it.uid in turno.idDipendenti }
    }

    /**
     * Verifica se il turno corrente ha modifiche non salvate
     */
    fun haModificheNonSalvate(): Boolean {
        val turno = _uiState.value.turnoInModifica ?: return false
        return turno.id.isEmpty() || turno.updatedAt.isAfter(turno.createdAt)
    }

    /**
     * Calcola la durata totale del turno corrente
     */
    fun calcolaDurataTurnoCorrente(): String {
        val turno = _uiState.value.turnoInModifica ?: return "0h 0m"

        val durataLavoro = turno.calcolaDurata()
        val durataPause = turno.pause.sumOf { it.durata.toMinutes() }
        val durataEffettiva = durataLavoro * 60 - durataPause

        val ore = durataEffettiva / 60
        val minuti = durataEffettiva % 60

        return "${ore}h ${minuti}m"
    }
}

// ========== SEALED CLASS PER VALIDAZIONE ==========

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

// ========== AGGIORNAMENTO MANAGERSTATE ==========

