package com.bizsync.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.domain.Azienda
import com.bizsync.model.sealedClass.RuoliAzienda
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


    private val _idAzienda = MutableStateFlow<String>("")
    val idAzienda : StateFlow<String> = _idAzienda


    fun aggiungiAzienda(idUtente: String, userViewModel: UserViewModel) {

        viewModelScope.launch(Dispatchers.IO){
            var loaded  = aziendaRepository.creaAzienda(Azienda("", nomeAzienda.value))

            if (loaded!=null)
            {
                _idAzienda.value = loaded
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID AZIENDA"  + idAzienda.toString())
            Log.d("AZIENDA_DEBUG", "VEDO SE CE L'ID UTENTE"  + idUtente)


                    var check =
                        userRepository.aggiornaAzienda(_idAzienda.value, idUtente, RuoliAzienda.Proprietario)

                    if (check) {
                        userViewModel.onAddAziendaRole(RuoliAzienda.Proprietario, _idAzienda.value)
                    } else {
                        error("Errore")
                    }
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
