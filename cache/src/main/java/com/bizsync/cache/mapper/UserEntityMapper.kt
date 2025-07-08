package com.bizsync.cache.mapper


import com.bizsync.cache.entity.UserEntity
import com.bizsync.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        uid = uid,
        email = email,
        nome = nome,
        cognome = cognome,
        photourl = photourl,
        idAzienda = idAzienda,
        manager = isManager,
        posizioneLavorativa = posizioneLavorativa,
        dipartimento = dipartimento
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        uid = uid,
        email = email,
        nome = nome,
        cognome = cognome,
        photourl = photourl,
        idAzienda = idAzienda,
        isManager = manager,
        posizioneLavorativa = posizioneLavorativa,
        dipartimento = dipartimento
    )
}
