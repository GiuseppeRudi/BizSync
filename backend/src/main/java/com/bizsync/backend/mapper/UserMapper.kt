package com.bizsync.backend.mapper

import android.util.Log
import com.bizsync.backend.dto.UserDto
import com.bizsync.domain.model.User

object UserMapper {

    // Domain → DTO (per salvare su Firebase)
    fun toDto(domain: User): UserDto {

        return UserDto(
            uid = domain.uid,
            email = domain.email,
            nome = domain.nome,
            cognome = domain.cognome,
            photourl = domain.photourl,
            idAzienda = domain.idAzienda,
            manager = domain.isManager,
            posizioneLavorativa = domain.posizioneLavorativa,
            dipartimento = domain.dipartimento,
            numeroTelefono = domain.numeroTelefono,
            indirizzo = domain.indirizzo,
            codiceFiscale = domain.codiceFiscale,
            dataNascita = domain.dataNascita,
            luogoNascita = domain.luogoNascita
        )
    }

    // DTO → Domain (per leggere da Firebase)
    fun toDomain(dto: UserDto): User {
        return User(
            uid = dto.uid,
            email = dto.email,
            nome = dto.nome,
            cognome = dto.cognome,
            photourl = dto.photourl,
            idAzienda = dto.idAzienda,
            isManager = dto.manager,
            posizioneLavorativa = dto.posizioneLavorativa,
            dipartimento = dto.dipartimento,
            numeroTelefono = dto.numeroTelefono,
            indirizzo = dto.indirizzo,
            codiceFiscale = dto.codiceFiscale,
            dataNascita = dto.dataNascita,
            luogoNascita = dto.luogoNascita
        )
    }

    fun toDtoList(domains: List<User>): List<UserDto> =
        domains.map { toDto(it) }

    fun toDomainList(dtos: List<UserDto>): List<User> =
        dtos.map { toDomain(it) }
}

// Extension functions per chiamare più in modo fluido
fun User.toDto(): UserDto = UserMapper.toDto(this)
fun UserDto.toDomain(): User = UserMapper.toDomain(this)

fun List<User>.toDtoList(): List<UserDto> = UserMapper.toDtoList(this)
fun List<UserDto>.toDomainList(): List<User> = UserMapper.toDomainList(this)
