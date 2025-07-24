package com.bizsync.ui.mapper

import com.bizsync.domain.model.User
import com.bizsync.ui.model.UserUi


object UserUiMapper {

    fun toDomain(ui: UserUi): User {
        return User(
            uid = ui.uid,
            email = ui.email,
            nome = ui.nome,
            cognome = ui.cognome,
            photourl = ui.photourl,
            idAzienda = ui.idAzienda,
            isManager = ui.isManager,
            posizioneLavorativa = ui.posizioneLavorativa,
            dipartimento = ui.dipartimento,
            numeroTelefono = ui.numeroTelefono,
            indirizzo = ui.indirizzo,
            codiceFiscale = ui.codiceFiscale,
            dataNascita = ui.dataNascita,
            luogoNascita = ui.luogoNascita
        )
    }

    fun toUi(domain: User): UserUi {
        return UserUi(
            uid = domain.uid,
            email = domain.email,
            nome = domain.nome,
            cognome = domain.cognome,
            photourl = domain.photourl,
            idAzienda = domain.idAzienda,
            isManager = domain.isManager,
            posizioneLavorativa = domain.posizioneLavorativa,
            dipartimento = domain.dipartimento,
            numeroTelefono = domain.numeroTelefono,
            indirizzo = domain.indirizzo,
            codiceFiscale = domain.codiceFiscale,
            dataNascita = domain.dataNascita,
            luogoNascita = domain.luogoNascita
        )
    }

    fun toDomainList(uiList: List<UserUi>): List<User> = uiList.map { toDomain(it) }
    fun toUiList(domainList: List<User>): List<UserUi> = domainList.map { toUi(it) }
}

fun UserUi.toDomain(): User = UserUiMapper.toDomain(this)
fun User.toUi(): UserUi = UserUiMapper.toUi(this)
fun List<UserUi>.toDomainList(): List<User> = UserUiMapper.toDomainList(this)
fun List<User>.toUiList(): List<UserUi> = UserUiMapper.toUiList(this)
