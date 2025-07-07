package com.bizsync.backend.mapper

import com.bizsync.backend.dto.UserDto
import com.bizsync.domain.model.User

object UserMapper {

    fun toDto(user: User): UserDto {
        return UserDto(
            uid = user.uid ,
            email = user.email,
            nome = user.nome,
            cognome = user.cognome,
            photourl = user.photourl,
            idAzienda = user.idAzienda,
            manager = user.manager,
            posizioneLavorativa = user.posizioneLavorativa,
            dipartimento = user.dipartimento
        )
    }

    fun toDomain(userDto: UserDto): User {
        return User(
            uid = userDto.uid,
            email = userDto.email,
            nome = userDto.nome,
            cognome = userDto.cognome,
            photourl = userDto.photourl,
            idAzienda = userDto.idAzienda,
            manager = userDto.manager,
            posizioneLavorativa = userDto.posizioneLavorativa,
            dipartimento = userDto.dipartimento
        )
    }
}


fun User.toDto(): UserDto = UserMapper.toDto(this)
fun UserDto.toDomain(): User = UserMapper.toDomain(this)