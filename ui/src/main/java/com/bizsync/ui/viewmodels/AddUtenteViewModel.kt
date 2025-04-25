package com.bizsync.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddUtenteViewModel  @Inject constructor(private val userRepository: UserRepository): ViewModel() {
    var currentStep = mutableStateOf(1)
    var uid = mutableStateOf("")

    var nome = mutableStateOf(" Ciccio ")
    var email  = mutableStateOf("")
    var cognome = mutableStateOf(" Pasticcio ")
    var photourl = mutableStateOf<Uri?>(null)


    fun addUser()
    {
        viewModelScope.launch {
            userRepository.addUser(User("",email.value,nome.value,cognome.value,photourl.value), uid.value)
        }
    }

}
