package com.bizsync.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.UserRepository
import com.bizsync.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    var user = mutableStateOf<User?>(null)


    var uid = mutableStateOf<String>("ciao")

    fun getUser(userId: String)
    {
        viewModelScope.launch {
             user.value = userRepository.getUserById(userId)
        }
    }

}