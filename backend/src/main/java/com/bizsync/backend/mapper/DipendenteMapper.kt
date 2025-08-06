package com.bizsync.backend.mapper


import com.bizsync.backend.dto.DipendenteDto
import com.bizsync.domain.model.User

object DipendenteMapper {

    // Domain → DTO (Dipendente)
    fun toDipendenteDto(domain: User): DipendenteDto {
        return DipendenteDto(
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

    // DTO → Domain
    fun toUserDomain(dto: DipendenteDto): User {
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

    fun toDtoList(domains: List<User>): List<DipendenteDto> =
        domains.map { toDipendenteDto(it) }

    fun toDomainList(dtos: List<DipendenteDto>): List<User> =
        dtos.map { toUserDomain(it) }
}

// Extension functions
fun User.toDipendenteDto(): DipendenteDto = DipendenteMapper.toDipendenteDto(this)
fun DipendenteDto.toDomain(): User = DipendenteMapper.toUserDomain(this)

fun List<User>.toDipendenteDtoList(): List<DipendenteDto> = DipendenteMapper.toDtoList(this)
fun List<DipendenteDto>.toUserDomainList(): List<User> = DipendenteMapper.toDomainList(this)
