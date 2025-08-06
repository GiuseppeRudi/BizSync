package com.bizsync.domain.model

data class TurnoWithUsers(
    val turno: Turno,
    val users: List<User>
)