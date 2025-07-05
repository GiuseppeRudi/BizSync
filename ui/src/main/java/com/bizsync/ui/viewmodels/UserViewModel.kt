package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.constants.sealedClass.Resource.*
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.Invito
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.bizsync.domain.utils.toDomain
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toUiState
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.model.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository, private val aziendaRepository: AziendaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UserState())
    val uiState: StateFlow<UserState> = _uiState


    fun onAddAziendaRole(idAzienda: String) {

        viewModelScope.launch {

            val idUtente = _uiState.value.user.uid
            val result = userRepository.aggiornaAzienda(idAzienda, idUtente, RuoliAzienda.Proprietario)

            when (result) {
                is Success -> {
                    _uiState.update {
                        it.copy(
                            user = _uiState.value.user.copy
                                (
                                idAzienda = idAzienda,
                                ruolo = RuoliAzienda.Proprietario.route,
                                isManager = RuoliAzienda.Proprietario.isPrivileged
                            ), hasLoadedAgency = true
                        )
                    }
                }
                is Error -> { _uiState.update { it.copy(resultMsg = result.message) } }
                else -> { _uiState.update { it.copy(resultMsg = "Errore sconosciuto") }}
            }

        }


    }


    fun clearMessage() {
        _uiState.update { it.copy(resultMsg = null, statusMsg = DialogStatusType.ERROR) }
    }


    fun checkUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val result = userRepository.getUserById(userId)

            Log.d("LOGINREPO_DEBUG", result.toString())


             when (result) {
                is Success -> _uiState.update {
                    it.copy(
                        user = result.data.toUiState(),
                        hasLoadedUser = true
                    )
                }

                is Error -> _uiState.update { it.copy(resultMsg = result.message) }
                is Empty -> _uiState.update { it.copy(resultMsg = "Utente non trovato") }
                else -> _uiState.update { it.copy(resultMsg = "Errore sconosciuto") }
            }

            val idAzienda = _uiState.value.user.idAzienda

            if (idAzienda.isNotEmpty() && _uiState.value.hasLoadedUser) {

                val loaded = aziendaRepository.getAziendaById(idAzienda)

                when (loaded) {
                    is Success -> {
                        _uiState.update {
                            it.copy(
                                azienda = loaded.data.toUiState(),
                                hasLoadedAgency = true
                            )
                        }
                        _uiState.update { it.copy(checkUser = true) }
                    }
                    is Error -> {_uiState.update { it.copy(resultMsg = loaded.message) } }
                    is Empty -> {  _uiState.update { it.copy(resultMsg = "Azienda non trovata") } }
                    else -> {_uiState.update { it.copy(resultMsg = "Errore sconosciuto") }
                }
            }

            if (idAzienda.isEmpty() && _uiState.value.hasLoadedUser) {
                _uiState.update { it.copy(checkUser = false) }
            }

        }
       }
    }

        fun clearError()
        {
            _uiState.update { it.copy(resultMsg = null) }}


        fun onAcceptInvite(invite: InvitoUi) {


            viewModelScope.launch {

                val uid = _uiState.value.user.uid
                val result = userRepository.updateAcceptInvite(invite.toDomain(), uid)


                if (result is Success) {

                    val currentUser = _uiState.value.user

                    _uiState.update {
                        it.copy(
                            user =
                                currentUser.copy(
                                    idAzienda = invite.idAzienda,
                                    isManager = invite.manager,
                                    ruolo = invite.posizioneLavorativa
                                )
                        )
                    }


                    val idAzienda = _uiState.value.user.idAzienda

                    val azienda = aziendaRepository.getAziendaById(idAzienda)

                    when (azienda) {
                        is Success -> {
                            _uiState.update {
                                it.copy(
                                    azienda = azienda.data.toUiState(),
                                    hasLoadedAgency = true, resultMsg = "Invito accettato con successo. Complimenti",
                                    statusMsg = DialogStatusType.SUCCESS, checkAcceptInvite = true
                                )
                            }
                        }

                        is Error -> {
                            _uiState.update { it.copy(resultMsg = azienda.message) }
                        }

                        is Empty -> {
                            _uiState.update { it.copy(resultMsg = "Azienda non trovata", statusMsg = DialogStatusType.ERROR) }
                        }
                        else -> {
                            _uiState.update { it.copy(resultMsg = "Errore sconosciuto", statusMsg = DialogStatusType.ERROR) }
                        }
                    }


                } else {
                    _uiState.update { it.copy(resultMsg = "Errore nell'accettare l'invito ", statusMsg = DialogStatusType.ERROR) }
                }

            }
        }

        fun updateTurniAree(aree: List<AreaLavoro>, turni: List<TurnoFrequente>) {

            val currentAgency = _uiState.value.azienda
            _uiState.update {
                it.copy(
                    azienda = currentAgency.copy(
                        areeLavoro = aree,
                        turniFrequenti = turni
                    )
                )
            }

        }


        fun clear() {
            _uiState.value = UserState()
        }

        fun aggiornaAzienda(idAzienda: String) {
            val currentUser = _uiState.value.user
            _uiState.update { it.copy(user = currentUser.copy(idAzienda = idAzienda)) }
        }

        fun change() {

            _uiState.update { it.copy(checkUser = true) }
        }

}