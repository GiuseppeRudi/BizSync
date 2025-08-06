package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.TimbratureWithUser
import com.bizsync.domain.repository.TimbraturaLocalRepository
import com.bizsync.domain.repository.UserLocalRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoadRecentTimbratureUseCase @Inject constructor(
    private val timbraturaLocalRepository: TimbraturaLocalRepository,
    private val userLocalRepository: UserLocalRepository
) {
    suspend operator fun invoke(limit: Int = 10): Resource<List<TimbratureWithUser>> {
        return try {
            val recentTimbrature = timbraturaLocalRepository.getRecentTimbrature(limit).first()
            val users = userLocalRepository.getDipendentiFull()

            // ✅ Business logic nel Use Case
            val timbratureWithUsers = recentTimbrature.mapNotNull { timbratura ->
                val user = users.find { it.uid == timbratura.idDipendente }
                if (user != null) {
                    TimbratureWithUser(timbratura, user)
                } else null
            }

            Resource.Success(timbratureWithUsers)
        } catch (e: Exception) {
            Resource.Error("Errore nel caricamento timbrature: ${e.message}")
        }
    }
}