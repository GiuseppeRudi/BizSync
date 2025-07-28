package com.bizsync.backend.hash.extensions

import com.bizsync.backend.dto.UserDto
import com.bizsync.backend.hash.HashManager
import com.bizsync.cache.entity.UserEntity
import com.bizsync.domain.model.User

import com.bizsync.backend.mapper.toDomain
import com.bizsync.cache.mapper.UserEntityMapper.toDomain

// FUNZIONE HASH SEMPLICE PER DOMAIN USER
fun User.generateUserHash(): String {
    val userString = listOf(
        uid,
        email,
        nome,
        cognome,
        photourl,
        idAzienda,
        isManager.toString(),
        posizioneLavorativa,
        dipartimento,
        numeroTelefono,
        indirizzo,
        codiceFiscale,
        dataNascita,
        luogoNascita
    ).joinToString("|")

    return HashManager.generateHash(userString)
}

// HASH PER LISTE - SEMPRE TRAMITE DOMAIN
fun List<UserDto>.generateFirebaseHash(): String {
    // DTO → Domain → Hash
    val domainUsers = this.map { it.toDomain() }
    return generateDomainUsersHash(domainUsers)
}

fun List<UserEntity>.generateCacheHash(): String {
    // Entity → Domain → Hash
    val domainUsers = this.map { it.toDomain() }
    return generateDomainUsersHash(domainUsers)
}

fun List<User>.generateDomainHash(): String {
    // Già Domain → Hash diretto
    return generateDomainUsersHash(this)
}

private fun generateDomainUsersHash(users: List<User>): String {
    return HashManager.generateHashFromList(users) { user ->
        user.generateUserHash()
    }
}
