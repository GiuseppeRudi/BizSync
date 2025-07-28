package com.bizsync.backend.mapper


import com.bizsync.backend.dto.MessageDto
import com.bizsync.domain.constants.enumClass.MessageType
import com.bizsync.domain.model.Message
import java.util.Date

object MessageMapper {

    fun toDomain(dto: MessageDto, currentUserId: String): Message {
        return Message(
            id = dto.id,
            senderId = dto.senderId,
            senderNome = dto.senderNome,
            content = dto.content,
            timestamp = dto.timestamp?.toDate() ?: Date(),
            tipo = when (dto.tipo) {
                "announcement" -> MessageType.ANNOUNCEMENT
                "system" -> MessageType.SYSTEM
                else -> MessageType.TEXT
            },
            isLetto = currentUserId in dto.lettoDa,
            categoria = dto.categoria
        )
    }

    fun toDomainList(dtoList: List<MessageDto>, currentUserId: String): List<Message> {
        return dtoList.map { toDomain(it, currentUserId) }
    }
}

fun MessageDto.toDomain(currentUserId: String): Message = MessageMapper.toDomain(this, currentUserId)
fun List<MessageDto>.toDomainList(currentUserId: String): List<Message> = MessageMapper.toDomainList(this, currentUserId)
