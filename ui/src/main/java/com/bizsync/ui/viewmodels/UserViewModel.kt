package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.domain.Azienda
import com.bizsync.model.domain.Invito
import com.bizsync.model.domain.User
import com.bizsync.model.sealedClass.RuoliAzienda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository, private val aziendaRepository: AziendaRepository) : ViewModel() {

    private val _user = MutableStateFlow<User>(User())
    val user : StateFlow<User> = _user

    private val _azienda = MutableStateFlow<Azienda>(Azienda())
    val azienda : StateFlow<Azienda> = _azienda

    fun onAziendaChanged(newValue : Azienda)
    {
        _azienda.value = newValue
    }

    private val _uid = MutableStateFlow<String>("nullo")
    val uid : StateFlow<String> = _uid

    fun onUserChanged(newValue : User)
    {
        _user.value = newValue
    }

    fun onUidChanged(newValue : String)
    {
        _uid.value = newValue
    }

    private var _check = MutableStateFlow<Boolean?>(null)
    var check : StateFlow<Boolean?> = _check


    suspend fun getUser(userId: String)
    {
             var loaded = userRepository.getUserById(userId)

             if(loaded!=null)
             {
                 _user.value = loaded
                 _uid.value= userId
             }

            else {
                //MANDARE IN ERORRE
             }

            Log.d("LOGINREPO_DEBUG", user.toString())
    }

    fun onAddAziendaRole(ruolo : RuoliAzienda, azienda: String)
    {
        _user.value = _user.value.copy(idAzienda = azienda, ruolo = ruolo.route, manager = ruolo.isPrivileged)
    }

    fun checkUser(userId : String)
    {
        viewModelScope.launch(Dispatchers.IO){
            var controllo = userRepository.checkUser(userId)

            Log.d("CHECK",  controllo.toString())
            if(controllo)
            {getUser(userId)}

            if(user.value.idAzienda.isNotEmpty() && controllo)
            {
                var loaded = aziendaRepository.getAziendaById(_user.value.idAzienda.toString())

                if (loaded!=null)
                {
                    _azienda.value = loaded
                }
                else
                {
                    //GESTIRE
                }
            }

            if(user.value.idAzienda.isEmpty() && controllo)
            {   Log.d("CHECK", "VEDIAMO IDAZIENDA" + user.value?.idAzienda.toString())
                controllo= false }

            _check.value = controllo
        }
    }

    suspend fun onAcceptInvite(invite : Invito)
    {
        // GESTIRE MEGLIO I CONTROLLI

        _user.value.idAzienda = invite.azienda
        _user.value.manager = invite.manager
        _user.value.ruolo = invite.nomeRuolo

        fetchAzienda()
    }

    suspend  fun fetchAzienda(){

        var loaded  = aziendaRepository.getAziendaById(_user.value.idAzienda)

        if (loaded != null)
        {
            _azienda.value = loaded

        }

        else
        {
            // GESTIRE
        }
    }

    fun clear() {
        _user.value = User()
        _azienda.value = Azienda()
        _uid.value = ""
        _check.value = null
    }

    fun aggiornaAzienda(idAzienda : String)
    {
        // GESTIRE MEGLIO
        _user.value.idAzienda = idAzienda
    }

    fun change()
    {
        _check.value = true
    }

}