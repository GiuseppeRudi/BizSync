package com.bizsync.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bizsync.backend.repository.AziendaRepository
import com.bizsync.backend.repository.ContractRepository
import com.bizsync.backend.repository.UserRepository
import com.bizsync.domain.constants.sealedClass.Resource.*
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.domain.model.TurnoFrequente
import com.bizsync.domain.constants.sealedClass.RuoliAzienda
import com.bizsync.domain.model.Contratto
import com.bizsync.domain.utils.toDomain
import com.bizsync.ui.components.DialogStatusType
import com.bizsync.ui.mapper.toUi
import com.bizsync.ui.model.InvitoUi
import com.bizsync.ui.model.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val aziendaRepository: AziendaRepository,
    private val contractRepository: ContractRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UserState())
    val uiState: StateFlow<UserState> = _uiState

    fun setGiornoPublicazioneTurni(giorno: DayOfWeek) {
        _uiState.update { it.copy(azienda = _uiState.value.azienda.copy(giornoPubblicazioneTurni = giorno)) }
    }

    fun aggiornaUser(modifiche : EditableUserFields)
    {
        val currentUser = _uiState.value.user

        _uiState.update {
            it.copy(
                user = currentUser.copy(
                    numeroTelefono = modifiche.numeroTelefono,
                    luogoNascita = modifiche.luogoNascita,
                    codiceFiscale = modifiche.codiceFiscale,
                    dataNascita = modifiche.dataNascita,
                    indirizzo = modifiche.indirizzo))

        }
    }

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
                                posizioneLavorativa = RuoliAzienda.Proprietario.route,
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

    fun changeContract(contratto: Contratto) {
        _uiState.update { it.copy(contratto = contratto) }
    }

    fun checkUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.getUserById(userId)

            when (result) {
                is Success -> {
                    val userUi = result.data.toUi()

                    _uiState.update {
                        it.copy(
                            user = userUi,
                            hasLoadedUser = true
                        )
                    }

                    val idAzienda = userUi.idAzienda
                    Log.d("VEDIAMO", "hasLoadedUser: true, idAzienda: $idAzienda")

                    if (idAzienda.isNotEmpty()) {
                        val loaded = aziendaRepository.getAziendaById(idAzienda)

                        when (loaded) {
                            is Success -> {
                                Log.d("VEDIAMO", "UI  $userUi")

                                if (!userUi.isManager) {
                                    val idUser = _uiState.value.user.uid
                                    val idAzienda = loaded.data.idAzienda

                                    val contrattoResult =
                                        contractRepository.getContrattoByUserAndAzienda(
                                            idUser,
                                            idAzienda
                                        )

                                    when (contrattoResult) {
                                        is Success -> {
                                            val contratto = contrattoResult.data

                                            _uiState.update {
                                                it.copy(
                                                    azienda = loaded.data.toUi(),
                                                    contratto = contratto, // salva se hai un campo nel UI state
                                                    hasLoadedAgency = true,
                                                    checkUser = true,
                                                    statusMsg = DialogStatusType.SUCCESS,
                                                    checkAcceptInvite = true
                                                )
                                            }
                                        }

                                        is Empty -> {
                                            _uiState.update {
                                                it.copy(
                                                    resultMsg = "Contratto non trovato per questo utente.",
                                                    statusMsg = DialogStatusType.ERROR
                                                )
                                            }
                                        }

                                        is Error -> {
                                            _uiState.update {
                                                it.copy(
                                                    resultMsg = contrattoResult.message,
                                                    statusMsg = DialogStatusType.ERROR
                                                )
                                            }
                                        }
                                    }
                                }
                                    else
                                    {
                                        _uiState.update {
                                            it.copy(
                                                azienda = loaded.data.toUi(),
                                                hasLoadedAgency = true,
                                                checkUser = true
                                            )
                                        }

                                    }
                                }

                            is Error -> _uiState.update { it.copy(resultMsg = loaded.message) }
                            is Empty -> _uiState.update { it.copy(resultMsg = "Azienda non trovata") }
                            else -> _uiState.update { it.copy(resultMsg = "Errore sconosciuto") }
                        }
                    } else {
                        _uiState.update { it.copy(checkUser = false) }
                        Log.d("VEDIAMO", "SONO QUA x2")
                    }
                }

                is Error -> _uiState.update { it.copy(resultMsg = result.message) }
                is Empty -> _uiState.update { it.copy(hasLoadedUser = true , checkUser = false) }
                else -> _uiState.update { it.copy(resultMsg = "Errore sconosciuto") }
            }
        }
    }


    fun clearError()
        {
            _uiState.update { it.copy(resultMsg = null) }}


        fun onAcceptInvite(invite: InvitoUi, contratto : Contratto) {


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
                                    posizioneLavorativa = invite.posizioneLavorativa,
                                    dipartimento = invite.dipartimento
                                ),
                            contratto = contratto
                        )
                    }


                    val idAzienda = _uiState.value.user.idAzienda

                    val azienda = aziendaRepository.getAziendaById(idAzienda)

                    when (azienda) {
                        is Success -> {
                            _uiState.update {
                                it.copy(
                                    azienda = azienda.data.toUi(),
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