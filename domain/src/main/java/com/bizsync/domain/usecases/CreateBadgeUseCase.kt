package com.bizsync.domain.usecases

import com.bizsync.domain.model.User
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.BadgeVirtuale
import com.bizsync.domain.repository.BadgeRepository
import javax.inject.Inject

class CreateBadgeUseCase @Inject constructor(
    private val badgeService: BadgeRepository
) {
    suspend operator fun invoke(user: User, azienda: Azienda): BadgeVirtuale {
        return badgeService.createBadgeVirtuale(user, azienda)
    }
}
