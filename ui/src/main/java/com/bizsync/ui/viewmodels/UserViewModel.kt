package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user

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
            _user.value = userRepository.getUserById(userId)
            _uid.value= userId
            Log.d("LOGINREPO_DEBUG", user.toString())
    }

    fun checkUser(userId : String)
    {
        viewModelScope.launch(Dispatchers.IO){
            var controllo = userRepository.checkUser(userId)

            Log.d("CHECK",  controllo.toString())
            if(controllo)
            {getUser(userId)}

            if(user.value?.idAzienda == "" && controllo)
            {   Log.d("CHECK", "VEDIAMO IDAZIENDA" + user.value?.idAzienda.toString())
                controllo= false }

            _check.value = controllo
        }
    }

    fun aggiornaAzienda(idAzienda : String)
    {
        _user.value?.idAzienda = idAzienda
    }

    fun change()
    {
        _check.value = true
    }

}