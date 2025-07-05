package com.bizsync.domain.utils

import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.model.Invito
import com.bizsync.ui.model.InvitoUi
import com.bizsync.domain.utils.DateUtils

object InvitoUiMapper {

    fun toUiState(invito: Invito): InvitoUi {
        val statoEnum = try {
            StatusInvite.valueOf(invito.stato)
        } catch (e: Exception) {
            StatusInvite.PENDING
        }

        return InvitoUi(
            id = invito.id,
            aziendaNome = invito.aziendaNome,
            email = invito.email,
            idAzienda = invito.idAzienda,
            manager = invito.manager,
            posizioneLavorativa = invito.nomeRuolo,
            dipartimento = invito.dipartimento,
            settoreAziendale = invito.settoreAziendale,
            tipoContratto = invito.tipoContratto,
            oreSettimanali = invito.oreSettimanali,
            ccnlInfo = invito.ccnlInfo,
            stato = statoEnum,

            // Conversione date da LocalDate a String per UI
            sentDate = with(DateUtils) { invito.sentDate.toUiString() },
            acceptedDate = invito.acceptedDate?.let {
                with(DateUtils) { it.toUiString() }
            } ?: ""
        )
    }

    fun toDomain(invitoUi: InvitoUi): Invito {
        return Invito(
            id = invitoUi.id,
            aziendaNome = invitoUi.aziendaNome,
            email = invitoUi.email,
            idAzienda = invitoUi.idAzienda,
            manager = invitoUi.manager,
            nomeRuolo = invitoUi.posizioneLavorativa,
            dipartimento = invitoUi.dipartimento,
            tipoContratto = invitoUi.tipoContratto,
            oreSettimanali = invitoUi.oreSettimanali,
            ccnlInfo = invitoUi.ccnlInfo,
            stato = invitoUi.stato.name,
            settoreAziendale = invitoUi.settoreAziendale,

            // Conversione date da String a LocalDate
            sentDate = with(DateUtils) {
                invitoUi.sentDate.parseUiDate() ?: todayUTC()
            },
            acceptedDate = if (invitoUi.acceptedDate.isNotEmpty()) {
                with(DateUtils) {
                    invitoUi.acceptedDate.parseUiDate()
                }
            } else null
        )
    }

    fun toUiStateList(inviti: List<Invito>): List<InvitoUi> {
        return inviti.map { toUiState(it) }
    }


}

// Extension functions aggiornate
fun Invito.toUiState(): InvitoUi =
    InvitoUiMapper.toUiState(this,)

fun InvitoUi.toDomain(): Invito =
    InvitoUiMapper.toDomain(this)

fun List<Invito>.toUiStateList(): List<InvitoUi> =
    InvitoUiMapper.toUiStateList(this)

