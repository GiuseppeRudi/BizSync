package com.bizsync.model.domain

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AreaLavoro(
    val id: String = UUID.randomUUID().toString(),
    var nomeArea: String = "",

)
{
    constructor() : this(UUID.randomUUID().toString(),"")
}