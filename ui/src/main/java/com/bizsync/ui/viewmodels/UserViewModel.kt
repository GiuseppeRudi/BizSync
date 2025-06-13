package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.Invito
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.model.User
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.bizsync.ui.mapper.toUiState
import com.bizsync.ui.model.AziendaUi
import com.bizsync.ui.model.UserState
import com.bizsync.ui.model.UserUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository, private val aziendaRepository: AziendaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UserState(UserUi(), AziendaUi()))
    val uiState : StateFlow<UserState> = _uiState

//    private val _user = MutableStateFlow<UserUi>(UserUi())
//    val user : StateFlow<UserUi> = _user
//
//    private val _azienda = MutableStateFlow<Azienda>(Azienda())
//    val azienda : StateFlow<Azienda> = _azienda
//
//    fun onAziendaChanged(newValue : Azienda)
//    {
//        _azienda.update { newValue }
//    }

//    private val _uid = MutableStateFlow<String>("nullo")
//    val uid : StateFlow<String> = _uid

//    fun onUserChanged(newValue : User)
//    {
//        _user.value = newValue
//    }
//
//    fun onUidChanged(newValue : String)
//    {
//        _uid.value = newValue
//    }

//    private var _check = MutableStateFlow<Boolean?>(null)
//    var check : StateFlow<Boolean?> = _check
//

    fun onUidChanged(newUid: String){
        _uiState.update { it.copy(user = _uiState.value.user.copy(uid = newUid)) }
    }

    suspend fun getUser(userId: String)
    {
        Log.d("LOGINREPO_DEBUG", userId)

        var loaded = userRepository.getUserById(userId)

        Log.d("LOGINREPO_DEBUG", loaded.toString())

        if(loaded!=null)
             {
                 _uiState.update { it.copy(user = loaded.toUiState()) }
                 //_user.value = loaded
//                 _uid.value= userId
             }

            else {
                //MANDARE IN ERORRE
             }

            Log.d("LOGINREPO_DEBUG", _uiState.value.user.toString())
    }

    fun onAddAziendaRole(ruolo : RuoliAzienda, azienda: String)
    {
       // _user.value = _user.value.copy(idAzienda = azienda, ruolo = ruolo.route, manager = ruolo.isPrivileged)
        _uiState.update { it.copy(user = _uiState.value.user.copy(idAzienda = azienda, ruolo = ruolo.route, isManager = ruolo.isPrivileged)) }
    }

    fun checkUser(userId : String)
    {
        viewModelScope.launch(Dispatchers.IO){
            var check = userRepository.checkUser(userId)

            Log.d("CHECK",  check.toString())
            if(check)
            {getUser(userId)}

            if(_uiState.value.user.idAzienda.isNotEmpty() && check)
            {
                var loaded = aziendaRepository.getAziendaById(_uiState.value.user.idAzienda.toString())

                if (loaded!=null)
                {
               //     _azienda.value = loaded
                    _uiState.update{ it.copy(azienda = loaded.toUiState())}
                }
                else
                {
                    //GESTIRE
                }
            }

            if(_uiState.value.user.idAzienda.isEmpty() && check)
            {   Log.d("CHECK", "VEDIAMO IDAZIENDA" + _uiState.value.user.idAzienda.toString())
                check= false }

            _uiState.update { it.copy(check = check) }
        }
    }

    suspend fun onAcceptInvite(invite : Invito)
    {
        // GESTIRE MEGLIO I CONTROLLI

//        _user.value.idAzienda = invite.azienda
//        _user.value.manager = invite.manager
//        _user.value.ruolo = invite.nomeRuolo
//
        _uiState.update{ it.copy(user = _uiState.value.user.copy(idAzienda = invite.azienda, isManager = invite.manager, ruolo = invite.nomeRuolo))}

        fetchAzienda()
    }

    fun updateTurniAree(aree: List<AreaLavoro>, turni: List<TurnoFrequente>) {

        _uiState.update { it.copy(azienda =
            _uiState.value.azienda.copy(areeLavoro = aree, turniFrequenti = turni))}
    }

    suspend  fun fetchAzienda(){

        var loaded  = aziendaRepository.getAziendaById(_uiState.value.azienda.idAzienda)

        if (loaded != null)
        {
           // _azienda.value = loaded

            _uiState.update { it.copy(azienda = loaded.toUiState()) }

        }

        else
        {
            // GESTIRE
        }
    }

    fun clear() {
        _uiState.value = UserState(UserUi(), AziendaUi())
    }

    fun aggiornaAzienda(idAzienda : String)
    {
        // GESTIRE MEGLIO
        //_user.value.idAzienda = idAzienda
        _uiState.update{ it.copy(user = _uiState.value.user.copy(idAzienda = idAzienda))}
    }

    fun change()
    {
        _uiState.update { it.copy(check = true) }
    }

}