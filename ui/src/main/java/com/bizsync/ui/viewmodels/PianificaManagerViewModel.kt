package com.bizsync.ui.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.orchestrator.TurnoOrchestrator
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.model.ManagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import com.bizsync.domain.model.Turno


@HiltViewModel
class PianificaManagerViewModel @Inject constructor(
    private val turnoOrchestrator: TurnoOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManagerState())
    val uiState: StateFlow<ManagerState> = _uiState


    fun  setLoading(loading: Boolean) {
     _uiState.update { it.copy( loading = loading) }
    }


    fun setTurniSettimanali(startWeek: LocalDate) {

        viewModelScope.launch {
            when (val result = turnoOrchestrator.fetchTurniSettimana(startWeek)) {
                is Resource.Success -> {


                    val turni = result.data

                    // Raggruppa i turni per giorno
                    val grouped = turni.groupBy { it.data.dayOfWeek }

                    // Crea una mappa con tutti i giorni della settimana, anche se vuoti
                    val allDays = DayOfWeek.values().associateWith { grouped[it] ?: emptyList() }


                    _uiState.update { current ->
                        current.copy(turniSettimanali = allDays)
                    }
                }
                is Resource.Error -> {
                    // Gestione errore: mappa con giorni vuoti
                    val allDays = DayOfWeek.values().associateWith { emptyList<Turno>() }
                    _uiState.update { current ->
                        current.copy(turniSettimanali = allDays)
                    }


                }
                is Resource.Empty -> {
                    // Nessun turno disponibile: mappa con giorni vuoti
                    val allDays = DayOfWeek.values().associateWith { emptyList<Turno>() }
                    _uiState.update { current ->
                        current.copy(turniSettimanali = allDays)
                    }

                    Log.d("SETTIMANA", "vuoto ")

                }
            }
        }
    }


    fun setTurniGiornalieri(dayOfWeek: DayOfWeek, dipartimentiDelGiorno: List<AreaLavoro>) {
        viewModelScope.launch {
            // Prendi lo stato corrente
            val currentTurniSettimanali = _uiState.value.turniSettimanali

            Log.d("TURNIGIORNALIERI", "TURNI SETTIMANA: ${currentTurniSettimanali}")

            // Lista di turni per il giorno (o lista vuota se non presente)
            val turniDelGiorno = currentTurniSettimanali[dayOfWeek] ?: emptyList()

            // Mappa dipartimentoId -> lista di turni di quel dipartimento quel giorno
            val turniPerDipartimento: Map<String, List<Turno>> = dipartimentiDelGiorno.associate { dipartimento ->
                dipartimento.id to turniDelGiorno.filter { it.dipartimentoId == dipartimento.id }
            }


            Log.d("TURNIGIORNALIERI", "PianificaCore: ${dipartimentiDelGiorno}")

            Log.d("TURNIGIORNALIERI", "PianificaCo+ùre: ${turniPerDipartimento}")

            // Aggiorna lo stato con la nuova mappa di turni giornalieri per dipartimento
            _uiState.update { current ->
                current.copy(turniGiornalieri = turniPerDipartimento, loading = false)
            }
        }
    }


    fun setTurniGiornalieriDipartimento(dipartimentoId: String) {
        _uiState.update { it.copy(turniGiornalieriDip = _uiState.value.turniGiornalieri[dipartimentoId]?: emptyList()) }
    }


}