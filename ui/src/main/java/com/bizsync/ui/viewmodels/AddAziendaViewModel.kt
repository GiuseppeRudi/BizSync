package com.bizsync.ui.viewmodels

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.ui.mapper.toDomain
import com.bizsync.ui.model.AddAziendaState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.DayOfWeek
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddAziendaViewModel @Inject constructor(
    private val aziendaRepository: AziendaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAziendaState())
    val uiState: StateFlow<AddAziendaState> = _uiState

    fun aggiungiAzienda(idUtente: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ADD_AZIENDA_DEBUG", "=== INIZIO CREAZIONE AZIENDA ===")

            val azienda = _uiState.value.azienda
            val indirizzoSelezionato = _uiState.value.indirizzoSelezionato

            Log.d("ADD_AZIENDA_DEBUG", "Dati azienda:")
            Log.d("ADD_AZIENDA_DEBUG", "  Nome: ${azienda.nome}")
            Log.d("ADD_AZIENDA_DEBUG", "  Latitudine: ${azienda.latitudine}")
            Log.d("ADD_AZIENDA_DEBUG", "  Longitudine: ${azienda.longitudine}")
            Log.d("ADD_AZIENDA_DEBUG", "  Indirizzo selezionato: ${indirizzoSelezionato?.getAddressLine(0)}")

            val result = aziendaRepository.creaAzienda(azienda.toDomain())

            when (result) {
                is Resource.Success -> {
                    Log.d("ADD_AZIENDA_DEBUG", "✅ Azienda creata con successo. ID: ${result.data}")
                    _uiState.update {
                        it.copy(
                            azienda = it.azienda.copy(idAzienda = result.data),
                            isAgencyAdded = true
                        )
                    }
                }
                is Resource.Error -> {
                    Log.e("ADD_AZIENDA_DEBUG", "❌ Errore creazione azienda: ${result.message}")
                    _uiState.update { it.copy(resultMsg = result.message) }
                }
                else -> {
                    Log.e("ADD_AZIENDA_DEBUG", "❌ Stato loading inaspettato")
                    _uiState.update { it.copy(resultMsg = "Errore nella creazione dell'azienda") }
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(resultMsg = null)
    }

    fun onSectorChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(sector = newValue)
        )
    }

    fun onNumDipendentiRangeChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(numDipendentiRange = newValue)
        )
    }

    fun onNomeAziendaChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(nome = newValue)
        )
    }

    fun onGiornoPubblicazioneChanged(newValue: DayOfWeek) {
        _uiState.value = _uiState.value.copy(
            azienda = _uiState.value.azienda.copy(giornoPubblicazioneTurni = newValue)
        )
    }

    // NUOVE FUNZIONI PER GESTIRE L'INDIRIZZO

    fun onIndirizzoChanged(newValue: String) {
        _uiState.update {
            it.copy(
                indirizzoInput = newValue,
                indirizziCandidati = emptyList(),
                indirizzoSelezionato = null,
                geocodingError = null
            )
        }
    }

    fun onIndirizzoSelezionato(address: Address) {
        Log.d("GEOCODING_DEBUG", "Indirizzo selezionato: ${address.getAddressLine(0)}")
        Log.d("GEOCODING_DEBUG", "Coordinate: ${address.latitude}, ${address.longitude}")

        _uiState.update {
            it.copy(
                indirizzoSelezionato = address,
                azienda = it.azienda.copy(
                    latitudine = address.latitude,
                    longitudine = address.longitude
                )
            )
        }
    }

    fun searchAddress(context: Context) {
        val query = _uiState.value.indirizzoInput.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(geocodingError = "Inserisci un indirizzo") }
            return
        }

        viewModelScope.launch {
            Log.d("GEOCODING_DEBUG", "=== INIZIO GEOCODIFICA ===")
            Log.d("GEOCODING_DEBUG", "Query: $query")

            _uiState.update {
                it.copy(
                    isGeocoding = true,
                    geocodingError = null,
                    indirizziCandidati = emptyList()
                )
            }

            try {
                val results = withContext(Dispatchers.IO) {
                    geocodeAddress(context, query)
                }

                Log.d("GEOCODING_DEBUG", "Risultati ottenuti: ${results.size}")

                when {
                    results.isEmpty() -> {
                        Log.w("GEOCODING_DEBUG", "Nessun risultato trovato")
                        _uiState.update {
                            it.copy(
                                isGeocoding = false,
                                geocodingError = "Indirizzo non trovato. Verifica che sia corretto e completo."
                            )
                        }
                    }
                    results.size == 1 -> {
                        Log.d("GEOCODING_DEBUG", "Risultato univoco - selezione automatica")
                        val address = results.first()
                        _uiState.update {
                            it.copy(
                                isGeocoding = false,
                                indirizziCandidati = results,
                                indirizzoSelezionato = address,
                                azienda = it.azienda.copy(
                                    latitudine = address.latitude,
                                    longitudine = address.longitude
                                )
                            )
                        }
                    }
                    else -> {
                        Log.d("GEOCODING_DEBUG", "Multipli risultati - richiesta selezione utente")
                        _uiState.update {
                            it.copy(
                                isGeocoding = false,
                                indirizziCandidati = results
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GEOCODING_DEBUG", "❌ Errore durante geocodifica: ${e.message}")
                _uiState.update {
                    it.copy(
                        isGeocoding = false,
                        geocodingError = "Errore durante la ricerca: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun geocodeAddress(context: Context, address: String): List<Address> {
        return try {
            if (!Geocoder.isPresent()) {
                throw IOException("Servizio di geocodifica non disponibile")
            }

            val geocoder = Geocoder(context, Locale.getDefault())

            // Richiedi fino a 5 possibili risultati
            val candidates = geocoder.getFromLocationName(address, 5) ?: emptyList()

            Log.d("GEOCODING_DEBUG", "Candidati trovati:")
            candidates.forEachIndexed { index, addr ->
                Log.d("GEOCODING_DEBUG", "  $index: ${addr.getAddressLine(0)}")
                Log.d("GEOCODING_DEBUG", "     Coordinate: ${addr.latitude}, ${addr.longitude}")
                Log.d("GEOCODING_DEBUG", "     Località: ${addr.locality}")
                Log.d("GEOCODING_DEBUG", "     CAP: ${addr.postalCode}")
                Log.d("GEOCODING_DEBUG", "     Paese: ${addr.countryName}")
            }

            // Filtra risultati con coordinate valide
            candidates.filter { addr ->
                addr.hasLatitude() && addr.hasLongitude() &&
                        addr.latitude != 0.0 && addr.longitude != 0.0
            }
        } catch (e: IOException) {
            Log.e("GEOCODING_DEBUG", "IOException durante geocodifica: ${e.message}")
            throw e
        }
    }

    // FUNZIONE PER VERIFICARE SE SI PUÒ PROCEDERE AL PROSSIMO STEP
    fun canProceedToNextStep(currentStep: Int): Boolean {
        val state = _uiState.value
        return when (currentStep) {
            1 -> state.azienda.nome.isNotBlank()
            2 -> state.azienda.numDipendentiRange.isNotBlank()
            3 -> state.azienda.sector.isNotBlank()
            4 -> true // Giorno pubblicazione ha valore default
            5 -> state.indirizzoSelezionato != null &&
                    state.azienda.latitudine != 0.0 &&
                    state.azienda.longitudine != 0.0
            else -> true
        }
    }

    fun onCurrentStepDown() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
    }

    fun onCurrentStepUp() {
        val canProceed = canProceedToNextStep(_uiState.value.currentStep)
        if (canProceed) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        }
    }
}