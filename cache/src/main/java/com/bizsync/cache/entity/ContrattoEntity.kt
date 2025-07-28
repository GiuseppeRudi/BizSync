package com.bizsync.cache.entity


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contratti")
data class ContrattoEntity(
    @PrimaryKey val id: String = "",
    val idDipendente: String = "",
    val idAzienda: String = "",
    val emailDipendente: String = "",
    val posizioneLavorativa: String = "",
    val dipartimento: String = "",
    val tipoContratto: String = "",
    val oreSettimanali: String = "",
    val settoreAziendale: String = "",
    val dataInizio: String = "",

    @Embedded val ccnlInfo: CcnlnfoEntity = CcnlnfoEntity(),

    val ferieUsate: Int = 0,             // in giorni
    val rolUsate: Int = 0,               // in ore
    val malattiaUsata: Int = 0           // in giorni
)

