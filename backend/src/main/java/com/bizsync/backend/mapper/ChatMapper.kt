package com.bizsync.backend.mapper

import com.bizsync.domain.constants.enumClass.ChatType


import com.bizsync.backend.dto.ChatDto
import com.bizsync.domain.model.Chat

object ChatMapper {

    fun toDomain(dto: ChatDto, currentUserId: String): Chat {
        return Chat(
            id = dto.id,
            tipo = when (dto.tipo) {
                "generale" -> ChatType.GENERALE
                "dipartimento" -> ChatType.DIPARTIMENTO
                "privata" -> ChatType.PRIVATA
                else -> ChatType.GENERALE
            },
            nome = dto.nome,
            idAzienda = dto.idAzienda,
            dipartimento = dto.dipartimento,
            partecipanti = dto.partecipanti,
            ultimoMessaggio = dto.ultimoMessaggio,
            ultimoMessaggioTimestamp = dto.ultimoMessaggioTimestamp?.toDate(),
            ultimoMessaggioSenderNome = dto.ultimoMessaggioSenderId,
            messaggiNonLetti = 0 // Calcolato separatamente
        )
    }

    fun toDomainList(dtoList: List<ChatDto>, currentUserId: String): List<Chat> {
        return dtoList.map { toDomain(it, currentUserId) }
    }
}

// Extension functions for convenient usage
fun ChatDto.toDomain(currentUserId: String): Chat = ChatMapper.toDomain(this, currentUserId)
fun List<ChatDto>.toDomainList(currentUserId: String): List<Chat> = ChatMapper.toDomainList(this, currentUserId)
