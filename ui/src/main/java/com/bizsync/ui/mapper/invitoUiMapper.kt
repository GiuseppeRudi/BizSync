import com.bizsync.domain.constants.enumClass.StatusInvite
import com.bizsync.domain.model.Invito
import com.bizsync.ui.model.InvitoUi

object InvitoUiMapper {

    fun toUiState(invito: Invito): InvitoUi {
        val statoEnum = try {
            StatusInvite.valueOf(invito.stato)
        } catch (e: Exception) {
            StatusInvite.INPENDING
        }
        return InvitoUi(
            id = invito.id,
            aziendaNome = invito.aziendaNome,
            email = invito.email,
            idAzienda = invito.idAzienda,
            manager = invito.manager,
            nomeRuolo = invito.nomeRuolo,
            stato = statoEnum
        )
    }

    fun toDomain(invitoUi: InvitoUi): Invito {
        return Invito(
            id = invitoUi.id,
            aziendaNome = invitoUi.aziendaNome,
            email = invitoUi.email,
            idAzienda = invitoUi.idAzienda,
            manager = invitoUi.manager,
            nomeRuolo = invitoUi.nomeRuolo,
            stato = invitoUi.stato.name
        )
    }

    fun toUiStateList(inviti: List<Invito>): List<InvitoUi> {
        return inviti.map { toUiState(it) }
    }
}

// Extension functions
fun Invito.toUiState(): InvitoUi = InvitoUiMapper.toUiState(this)
fun InvitoUi.toDomain(): Invito = InvitoUiMapper.toDomain(this)
fun List<Invito>.toUiStateList(): List<InvitoUi> = InvitoUiMapper.toUiStateList(this)
