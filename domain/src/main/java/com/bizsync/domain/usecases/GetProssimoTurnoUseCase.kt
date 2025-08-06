package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.ProssimoTurno
import com.bizsync.domain.repository.BadgeRepository
import javax.inject.Inject

class GetProssimoTurnoUseCase @Inject constructor(
    private val badgeService: BadgeRepository
) {
    suspend operator fun invoke(userId: String): Resource<ProssimoTurno> {
        return badgeService.getProssimoTurno(userId)
    }
}
