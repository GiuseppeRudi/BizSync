package com.bizsync.domain.usecases


import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Timbratura
import com.bizsync.domain.repository.BadgeRepository
import java.time.LocalDate
import javax.inject.Inject

class GetTimbratureGiornaliereUseCase @Inject constructor(
    private val badgeService: BadgeRepository
) {
    suspend operator fun invoke(userId: String, date: LocalDate): Resource<List<Timbratura>> {
        return badgeService.getTimbratureGiornaliere(userId, date)
    }
}
