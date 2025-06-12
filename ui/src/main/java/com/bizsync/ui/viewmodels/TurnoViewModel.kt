package com.bizsync.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.TurnoRepository
import com.bizsync.domain.model.Membro
import com.bizsync.domain.model.Turno
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TurnoViewModel @Inject constructor(private val turnoRepository: TurnoRepository) : ViewModel() {



    private val _text = MutableStateFlow("")
    val text : StateFlow<String> = _text

    fun onTextChanged(newValue : String)
    {
        _text.value = newValue
    }


    fun aggiungiturno(pianificaVM : PianificaViewModel, turno: Turno){
        viewModelScope.launch {
            val esito = turnoRepository.aggiungiTurno(turno)

            if(esito)
            {
                pianificaVM.addTurno(turno)
            }

            else
            {
                //GESTIRE L'ERRORE
            }
        }
    }

    val membriDiProva = listOf(
        Membro(
            id = "1",
            nome = "Marco",
            cognome = "Rossi",
            ruolo = "Manager",
            email = "marco.rossi@example.com",
            telefono = "1234567890"
        ),
        Membro(
            id = "2",
            nome = "Giulia",
            cognome = "Verdi",
            ruolo = "Sviluppatrice",
            email = "giulia.verdi@example.com",
            telefono = "0987654321"
        ),
        Membro(
            id = "3",
            nome = "Luca",
            cognome = "Bianchi",
            ruolo = "Designer",
            email = "luca.bianchi@example.com",
            telefono = "1122334455"
        ),
        Membro(
            id = "4",
            nome = "Anna",
            cognome = "Neri",
            ruolo = "HR",
            email = "anna.neri@example.com",
            telefono = "5566778899"
        ),
        Membro(
            id = "5",
            nome = "Paolo",
            cognome = "Ferrari",
            ruolo = "Tecnico",
            email = "paolo.ferrari@example.com",
            telefono = "6677889900",
            isAttivo = false // membro non attivo, non verrà mostrato nei risultati filtrati
        )
    )

}