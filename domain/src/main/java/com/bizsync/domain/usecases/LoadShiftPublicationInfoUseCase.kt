package com.bizsync.domain.usecases

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Azienda
import com.bizsync.domain.model.ShiftPublicationInfo
import com.bizsync.domain.repository.WeeklyShiftRepository
import com.bizsync.domain.utils.WeeklyPublicationCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class LoadShiftPublicationInfoUseCase @Inject constructor(
    private val weeklyShiftRepository: WeeklyShiftRepository
) {
    suspend operator fun invoke(azienda: Azienda): Resource<ShiftPublicationInfo> {
        return try {
            val weekStartRiferimento = WeeklyPublicationCalculator.getReferenceWeekStart(LocalDate.now())

            when (val publicationRecord = weeklyShiftRepository.getThisWeekPublishedShift(
                azienda.idAzienda,
                weekStartRiferimento
            )) {
                is Resource.Success -> {
                    val daysUntilPublication = daysUntilNextFriday(LocalDate.now())
                    val shiftsPublished = publicationRecord.data != null

                    Resource.Success(
                        ShiftPublicationInfo(
                            daysUntilShiftPublication = daysUntilPublication,
                            shiftsPublishedThisWeek = shiftsPublished
                        )
                    )
                }
                is Resource.Error -> {
                    Resource.Error("Errore caricamento info pubblicazione: ${publicationRecord.message}")
                }
                else -> {
                    Resource.Success(
                        ShiftPublicationInfo(
                            daysUntilShiftPublication = daysUntilNextFriday(LocalDate.now()),
                            shiftsPublishedThisWeek = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Resource.Error("Errore caricamento info pubblicazione: ${e.message}")
        }
    }

    private fun daysUntilNextFriday(date: LocalDate): Int {
        val todayValue = date.dayOfWeek.value        // lun=1 … dom=7
        val fridayValue = DayOfWeek.FRIDAY.value     // 5
        return (fridayValue - todayValue + 7) % 7
    }
}