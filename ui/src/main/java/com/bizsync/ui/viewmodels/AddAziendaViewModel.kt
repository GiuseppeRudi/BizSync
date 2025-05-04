package com.bizsync.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.Azienda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch


@HiltViewModel
class AddAziendaViewModel  @Inject constructor(private val aziendaRepository: AziendaRepository, private val userRepository: UserRepository): ViewModel() {


    private val _currentStep = MutableStateFlow(1)
    val currentStep : StateFlow<Int> = _currentStep


    private val _nomeAzienda = MutableStateFlow("Ciccio Industry")
    val nomeAzienda: StateFlow<String> = _nomeAzienda


    private val _numDipendentiRange = MutableStateFlow("")
    val numDipendentiRange: StateFlow<String> = _numDipendentiRange


    private val _sector = MutableStateFlow("")
    val sector: StateFlow<String> = _sector


    private val _customSector  = MutableStateFlow("")
    val customSector : StateFlow<String> = _customSector


    private val _idAzienda = MutableStateFlow<String?>("")
    val idAzienda : StateFlow<String?> = _idAzienda


    fun aggiungiAzienda(idUtente: String, userViewModel: UserViewModel) {

        viewModelScope.launch(Dispatchers.IO){
            _idAzienda.value = aziendaRepository.creaAzienda(Azienda("", nomeAzienda.value))

            var azienda = idAzienda.value
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID AZIENDA"  + idAzienda.toString())
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID UTENTE"  + idUtente)

            if (azienda != null) {
                userRepository.aggiornaAzienda(azienda, idUtente)
                userViewModel.user.value?.idAzienda = azienda
                Log.d("AZIENDA_DEBUG", "Campo aggiornato con successo")
            } else {
                Log.e("AZIENDA_DEBUG", "ID azienda è nullo")
            }

        }
    }


    fun onIdAzienda(newValue: String)
    {
        _idAzienda.value = newValue
    }

    fun onCustomSectorChanged(newValue : String)
    {
        _customSector.value = newValue
    }

    fun onSectorChanged(newValue: String) {
        _sector.value = newValue
    }

    fun onNumDipendentiRangeChanged(newValue: String) {
        _numDipendentiRange.value = newValue
    }

    fun onNomeAziendaChanged(newValue: String) {
        _nomeAzienda.value = newValue
    }

    fun onCurrentStepDown()
    {
        _currentStep.value = _currentStep.value - 1
    }

    fun onCurrentStepUp()
    {
        _currentStep.value = _currentStep.value + 1
    }
}
