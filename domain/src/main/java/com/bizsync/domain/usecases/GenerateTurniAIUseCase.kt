package com.bizsync.domain.usecases

import com.bizsync.domain.model.*
import com.bizsync.domain.repository.TurnoRemoteRepository
import java.time.LocalDate
import javax.inject.Inject

class GenerateTurniAIUseCase @Inject constructor(
    private val turniAIRemoteRepository: TurnoRemoteRepository
) {
    suspend operator fun invoke(
        dipartimento: AreaLavoro,
        giornoSelezionato: LocalDate,
        dipendentiDisponibili: DipendentiGiorno,
        statoSettimanale: Map<String, StatoSettimanaleDipendente>,
        turniEsistenti: List<Turno>,
        descrizioneAggiuntiva: String
    ): TurniGeneratiAI {
        return turniAIRemoteRepository.generateTurni(
            dipartimento, giornoSelezionato, dipendentiDisponibili,
            statoSettimanale, turniEsistenti, descrizioneAggiuntiva
        )
    }
}
