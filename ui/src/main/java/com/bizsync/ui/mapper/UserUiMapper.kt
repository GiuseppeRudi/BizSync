package com.bizsync.ui.mapper

import com.bizsync.domain.model.User
import com.bizsync.ui.model.UserUi

object UserUiMapper {

    fun toUiState(user: User): UserUi {
        return UserUi(
            uid = user.uid,
            email = user.email,
            nome = user.nome,
            cognome = user.cognome,
            photourl = user.photourl,
            idAzienda = user.idAzienda,
            isManager = user.manager,
            posizioneLavorativa = user.posizioneLavorativa,
            dipartimento = user.dipartimento
        )
    }

    fun toDomain(userUi: UserUi): User {
        return User(
            uid = userUi.uid,
            email = userUi.email,
            nome = userUi.nome,
            cognome = userUi.cognome,
            photourl = userUi.photourl,
            idAzienda = userUi.idAzienda,
            manager = userUi.isManager,
            posizioneLavorativa = userUi.posizioneLavorativa,
            dipartimento = userUi.dipartimento
        )
    }

    fun toUiStateList(users: List<User>): List<UserUi> {
        return users.map { toUiState(it) }
    }
}

fun User.toUiState(): UserUi = UserUiMapper.toUiState(this)
fun UserUi.toDomain(): User = UserUiMapper.toDomain(this)
fun List<User>.toUiStateList(): List<UserUi> = UserUiMapper.toUiStateList(this)
