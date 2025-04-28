package com.bizsync.ui.viewmodels

import android.util.Log
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
    var uid = mutableStateOf<String>("nullo")


    suspend fun getUser(userId: String)
    {
            user.value = userRepository.getUserById(userId)
            uid.value= userId
            Log.d("LOGINREPO_DEBUG", user.toString())
    }

    suspend fun checkUser(userId : String) : Boolean
    {
            return userRepository.checkUser(userId)
    }


}