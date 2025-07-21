package com.bizsync.cache.mapper


import com.bizsync.cache.entity.UserEntity
import com.bizsync.cache.mapper.UserEntityMapper.toDomain
import com.bizsync.cache.mapper.UserEntityMapper.toEntity
import com.bizsync.domain.model.User

object UserEntityMapper {

    fun UserEntity.toDomain(): User {
        return User(
            uid = uid,
            email = email,
            nome = nome,
            cognome = cognome,
            photourl = photourl,
            idAzienda = idAzienda,
            isManager = isManager,
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
            isManager = isManager,
            posizioneLavorativa = posizioneLavorativa,
            dipartimento = dipartimento
        )
    }
}

// CONVERSIONI LISTE - Entity → Domain
fun List<UserEntity>.toDomainList(): List<User> {
    return this.map { it.toDomain() }
}



// CONVERSIONI LISTE - Domain → Entity
fun List<User>.toEntityList(): List<UserEntity> {
    return this.map { it.toEntity() }
}
