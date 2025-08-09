package com.bizsync.ui.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.domain.constants.enumClass.AbsenceStatus
import com.bizsync.domain.constants.enumClass.TipoNota
import com.bizsync.domain.constants.enumClass.TipoPausa
import com.bizsync.domain.constants.enumClass.ZonaLavorativa
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.constants.sealedClass.ValidationResult
import com.bizsync.domain.model.*
import com.bizsync.domain.usecases.DeleteTurnoUseCase
import com.bizsync.domain.usecases.FetchTurniSettimaanaUseCase
import com.bizsync.domain.usecases.GenerateTurniAIUseCase
import com.bizsync.domain.usecases.GetLocalAbsenceUseCase
import com.bizsync.domain.usecases.GetLocalContrattiUseCase
import com.bizsync.domain.usecases.GetTurnoByIdUseCase
import com.bizsync.domain.usecases.InsertTurniUseCase
import com.bizsync.domain.usecases.InsertTurnoUseCase
import com.bizsync.domain.usecases.SaveTurnoUseCase
import com.bizsync.domain.usecases.TurnoExistsUseCase
import com.bizsync.domain.usecases.UpdateTurnoUseCase
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
    private val generateTurniAIUseCase: GenerateTurniAIUseCase,
    private val fetchTurniSettimaanaUseCase: FetchTurniSettimaanaUseCase,
    private val getTurnoByIdUseCase: GetTurnoByIdUseCase,
    private val turnoExistsUseCase: TurnoExistsUseCase,
    private val insertTurnoUseCase: InsertTurnoUseCase,
    private val updateTurnoUseCase: UpdateTurnoUseCase,
    private val saveTurnoUseCase: SaveTurnoUseCase,
    private val insertTurniUseCase: InsertTurniUseCase,
    private val getLocalAbsenceUseCase: GetLocalAbsenceUseCase,
    private val getLocalContrattiUseCase: GetLocalContrattiUseCase,
    private val deleteTurnoUseCase: DeleteTurnoUseCase
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

                val result = generateTurniAIUseCase(
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
                        idAzienda = idAzienda
                    )
                }

                _uiState.update {
                    it.copy(
                        isGeneratingTurni = false,
                        turniGeneratiAI = turniConvertiti,
                        showAIResultDialog = true,
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

        // Crea la mappa delle zone lavorative con default IN_SEDE per tutti i dipendenti
        val zoneLavorative = turnoAI.idDipendenti.associateWith { ZonaLavorativa.IN_SEDE }

        return Turno(
            id = UUID.randomUUID().toString(),
            titolo = turnoAI.titolo,
            idAzienda = idAzienda,
            idDipendenti = turnoAI.idDipendenti,
            dipartimento = dipartimento,
            data = giornoSelezionato,
            orarioInizio = LocalTime.parse(turnoAI.orarioInizio),
            orarioFine = LocalTime.parse(turnoAI.orarioFine),
            zoneLavorative = zoneLavorative, // Aggiungi la nuova proprietà
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
            insertTurniUseCase(turniAI)

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

            Log.d("TURNO_DEBUG", "✅ DAIIIII per $giorno e dipartimento $idDipartimento: ${_uiState.value.disponibilitaMembriTurno}}")
        }
    }

    fun inizializzaDatiDipendenti(dipendenti: List<User>) {
        _uiState.update { it.copy(dipendenti = dipendenti) }
    }

    fun inizializzaDatiWeeklyRiferimento(dipendenti: List<User>) {
        viewModelScope.launch {
            try {
                // LE ASSENZE MI SERVONO SOLO DELLA SETTIMANA DI RIFERIMENTO
                val assDeferred = async { getLocalAbsenceUseCase() }
                val contrDeferred = async { getLocalContrattiUseCase() }

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

    fun setDipendentiStato() {
        val listaDipendenti = _uiState.value.dipendenti
        val assenzeFiltrate = _uiState.value.assenze
        val contratti = _uiState.value.contratti

        val dipendentiSettimana = calcolaDipendentiSettimana(
            dipendenti = listaDipendenti,
            assenzeSettimana = assenzeFiltrate
        )
        Log.d(TAG, "caricamento dipendenti settimana: $dipendentiSettimana")
        _uiState.update { it.copy(dipendentiSettimana = dipendentiSettimana) }

        _uiState.update { it.copy(statoSettimanaleDipendenti = calcolaStatoSettimanaleDipendenti(listaDipendenti, contratti)) }

        Log.d(TAG, "caricamento dipendenti stato settimanale: ${_uiState.value.statoSettimanaleDipendenti}")
    }

    fun calcolaStatoSettimanaleDipendenti(
        dipendenti: List<User>,
        contratti: List<Contratto>
    ): Map<String, StatoSettimanaleDipendente> {
        return dipendenti.associate { user ->
            val contratto = contratti.find { it.idDipendente == user.uid }
            val maxOre: Int = toIntSafe(contratto?.oreSettimanali) ?: 0
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
                        // ASSENZA PARZIALE: Ha orari specifici
                        assenza.startTime != null && assenza.endTime != null && assenza.totalHours != null -> {
                            parziale = AssenzaParziale(
                                inizio = assenza.startTime!!,
                                fine = assenza.endTime!!
                            )
                        }

                        // ASSENZA TOTALE: totalDays > 0 (non solo != null!)
                        assenza.totalDays != null && assenza.totalDays!! > 0 -> {
                            isTotale = true
                            break
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
        when (val result = fetchTurniSettimaanaUseCase(startWeek)) {
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

//    fun setTurniGiornalieriDipartimento(dipartimentoId: String, dayOfWeek: DayOfWeek) {
//        val turniDelGiorno = _uiState.value.turniSettimanali[dayOfWeek] ?: emptyList()
//
//        val turniGiornalieriDip = turniDelGiorno.filter { turno ->
//            turno.dipartimento == dipartimentoId
//        }
//
//        _uiState.update {
//            it.copy(turniGiornalieriDip = turniGiornalieriDip)
//        }
//    }

    fun setTurniGiornalieriDipartimento(dipartimentoId: String) {
        val turniGiornalieriDip = _uiState.value.turniGiornalieri[dipartimentoId] ?: emptyList()

        _uiState.update {
            it.copy(turniGiornalieriDip = turniGiornalieriDip)
        }
    }

    fun setTurniSettimanali(startWeek: LocalDate, idAzienda: String) {
        viewModelScope.launch {
            Log.d("PianificaGiornata", "🚀 === setTurniSettimanali START ===")
            Log.d("PianificaGiornata", "📅 startWeek: $startWeek")
            Log.d("PianificaGiornata", "🏢 idAzienda: $idAzienda")
            Log.d("PianificaGiornata", "📊 Current turniSettimanali size BEFORE: ${_uiState.value.turniSettimanali.values.flatten().size}")

            // Inizio caricamento
            _uiState.update {
                Log.d("PianificaGiornata", "⏳ Setting isLoadingTurni = true")
                it.copy(isLoadingTurni = true)
            }

            Log.d("PianificaGiornata", "🌐 Calling fetchTurniSettimaanaUseCase...")

            when (val result = fetchTurniSettimaanaUseCase(startWeek, idAzienda)) {
                is Resource.Success -> {
                    val turni = result.data
                    Log.d("PianificaGiornata", "✅ SUCCESS: Fetched ${turni.size} turni from API")

                    // Log alcuni turni per debug
                    turni.take(3).forEachIndexed { index, turno ->
                        Log.d("PianificaGiornata", "   Turno $index: ${turno.titolo} - ${turno.data} (${turno.data.dayOfWeek})")
                    }

                    val grouped = turni.groupBy { it.data.dayOfWeek }
                    Log.d("PianificaGiornata", "📋 Grouped turni by day:")
                    grouped.forEach { (day, turniDay) ->
                        Log.d("PianificaGiornata", "   $day: ${turniDay.size} turni")
                    }

                    val allDays = DayOfWeek.entries.associateWith { grouped[it] ?: emptyList() }
                    Log.d("PianificaGiornata", "📊 Final allDays structure:")
                    allDays.forEach { (day, turniDay) ->
                        Log.d("PianificaGiornata", "   $day: ${turniDay.size} turni")
                    }

                    _uiState.update {
                        Log.d("PianificaGiornata", "💾 Updating state with SUCCESS data...")
                        val newState = it.copy(
                            turniSettimanali = allDays,
                            isLoadingTurni = false
                        )
                        Log.d("PianificaGiornata", "📊 New turniSettimanali size: ${newState.turniSettimanali.values.flatten().size}")
                        newState
                    }

                    Log.d("PianificaGiornata", "✅ SUCCESS update completed")
                }

                is Resource.Error -> {
                    Log.e("PianificaGiornata", "❌ ERROR: ${result.message}")
                    Log.e("PianificaGiornata", "❌ Setting turniSettimanali to EMPTY due to error")

                    val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }

                    _uiState.update {
                        Log.d("PianificaGiornata", "💾 Updating state with ERROR (empty data)...")
                        it.copy(
                            turniSettimanali = allDays,
                            isLoadingTurni = false
                        )
                    }

                    Log.e("PianificaGiornata", "❌ ERROR update completed - turniSettimanali now EMPTY")
                }

                is Resource.Empty -> {
                    Log.w("PianificaGiornata", "⚠️ EMPTY: No turni found for this week")
                    Log.w("PianificaGiornata", "⚠️ Setting turniSettimanali to EMPTY due to empty result")

                    val allDays = DayOfWeek.entries.associateWith { emptyList<Turno>() }

                    _uiState.update {
                        Log.d("PianificaGiornata", "💾 Updating state with EMPTY data...")
                        it.copy(
                            turniSettimanali = allDays,
                            isLoadingTurni = false
                        )
                    }

                    Log.w("PianificaGiornata", "⚠️ EMPTY update completed - turniSettimanali now EMPTY")
                }
            }

            Log.d("PianificaGiornata", "📊 Final turniSettimanali size AFTER: ${_uiState.value.turniSettimanali.values.flatten().size}")
            Log.d("PianificaGiornata", "🏁 === setTurniSettimanali END ===")
            Log.d("PianificaGiornata", "") // Riga vuota per separare
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

    fun pulisciPausaInModifica() {
        _uiState.update {
            it.copy(
                pausaInModifica = null,
                showAddEditPauseDialog = false
            )
        }
        Log.d(TAG, "Pulita pausa in modifica")
    }

    fun aggiornaTipoPausa(nuovoTipo: TipoPausa) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(tipo = nuovoTipo)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Tipo pausa aggiornato: $nuovoTipo")
    }

    fun aggiornaDurataPausa(nuovaDurata: Duration) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(durata = nuovaDurata)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Durata pausa aggiornata: ${nuovaDurata.toMinutes()} minuti")
    }

    fun aggiornaRetribuitaPausa(isRetribuita: Boolean) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(èRetribuita = isRetribuita)
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Retribuita pausa aggiornata: $isRetribuita")
    }

    fun aggiornaNotePausa(nuoveNote: String?) {
        val pausaCorrente = _uiState.value.pausaInModifica ?: return
        val pausaAggiornata = pausaCorrente.copy(note = nuoveNote?.takeIf { it.isNotBlank() })
        _uiState.update { it.copy(pausaInModifica = pausaAggiornata) }
        Log.d(TAG, "Note pausa aggiornate")
    }

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

    fun setShowPauseDialog(show: Boolean) {
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

    fun editTurno(turnoId: String) {
        viewModelScope.launch {
            try {
                setLoading(true)

                // Recupera il turno dal database locale
                val turno = getTurnoByIdUseCase(turnoId)

                if (turno != null) {
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
                val turno = getTurnoByIdUseCase(turnoId)

                if (turno != null) {
                    // Mostra dialog di conferma
                    _uiState.update {
                        it.copy(
                            turnoToDelete = turno,
                            showDeleteConfirmDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Errore durante la preparazione eliminazione: ${e.message}")
            }
        }
    }

    fun cancelDeleteTurno() {
        _uiState.update {
            it.copy(
                turnoToDelete = null,
                showDeleteConfirmDialog = false
            )
        }
    }

    fun deleteTurno(turnoId: String) {
        viewModelScope.launch {
            try {
                setLoading(true)

                when (val result = deleteTurnoUseCase(turnoId)) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ ${result.data}")
                        _uiState.update {
                            it.copy(
                                hasChangeShift = true,
                                successMessage = result.data
                            )
                        }
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore eliminazione: ${result.message}")
                        _uiState.update {
                            it.copy(
                                errorMessage = result.message
                            )
                        }
                    }

                    is Resource.Empty -> {
                        Log.w(TAG, "⚠️ Risposta vuota durante eliminazione")
                        _uiState.update {
                            it.copy(
                                errorMessage = "Errore imprevisto durante l'eliminazione"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "🚨 Errore imprevisto: ${e.message}")
                _uiState.update {
                    it.copy(
                        errorMessage = "Errore imprevisto: ${e.message}"
                    )
                }
            } finally {
                setLoading(false)
            }
        }
    }

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

    fun saveTurno(dipartimento: String, giornoSelezionato: LocalDate, idAzienda: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true) }

                val turnoRaw = _uiState.value.turnoInModifica

                // ✅ GENERA ID SE È UN NUOVO TURNO
                val turno = if (turnoRaw.id.isEmpty()) {
                    turnoRaw.withGeneratedId() // Genera ID per nuovo turno
                } else {
                    turnoRaw // Mantieni ID esistente per modifica
                }

                val isNuovoTurno = turnoRaw.id.isEmpty()
                Log.d(TAG, "💾 ${if (isNuovoTurno) "Creando nuovo turno" else "Modificando turno esistente"}: ${turno.id}")

                when (val result = saveTurnoUseCase(turno, dipartimento, giornoSelezionato, idAzienda)) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ ${result.data}")
                        _uiState.update {
                            it.copy(
                                hasChangeShift = true,
                                loading = false,
                                turnoInModifica = Turno(), // ✅ Reset a turno vuoto (ID vuoto)
                                successMessage = result.data
                            )
                        }
                    }

                    is Resource.Error -> {
                        Log.e(TAG, "❌ Errore salvataggio: ${result.message}")
                        _uiState.update {
                            it.copy(
                                loading = false,
                                errorMessage = result.message
                            )
                        }
                    }

                    is Resource.Empty -> {
                        Log.w(TAG, "⚠️ Risposta vuota durante salvataggio")
                        _uiState.update {
                            it.copy(
                                loading = false,
                                errorMessage = "Errore imprevisto durante il salvataggio"
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "🚨 Errore imprevisto: ${e.message}")
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = "Errore imprevisto: ${e.message}"
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

    fun getDipendentiSelezionati(): List<User> {
        val turno = _uiState.value.turnoInModifica
        val tuttiDipendenti = _uiState.value.dipendenti

        return tuttiDipendenti.filter { it.uid in turno.idDipendenti }
    }

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