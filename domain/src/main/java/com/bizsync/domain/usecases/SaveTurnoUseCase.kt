package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Turno
import com.bizsync.domain.repository.TurnoSyncRepository
import java.time.LocalDate
import javax.inject.Inject

class SaveTurnoUseCase @Inject constructor(
    private val turnoSyncRepository: TurnoSyncRepository
) {

    /**
     * Salva o aggiorna un turno
     * @param turno Turno da salvare
     * @param dipartimento Dipartimento di appartenenza
     * @param giornoSelezionato Data del turno
     * @param idAzienda ID dell'azienda
     * @return Resource con messaggio di successo o errore
     */
    suspend operator fun invoke(
        turno: Turno,
        dipartimento: String,
        giornoSelezionato: LocalDate,
        idAzienda: String
    ): Resource<String> {
        return turnoSyncRepository.saveTurno(turno, dipartimento, giornoSelezionato, idAzienda)
    }
}