package com.bizsync.domain.repository

import com.bizsync.domain.constants.sealedClass.Resource
import com.bizsync.domain.model.Absence
import java.time.LocalDate

interface AbsenceSyncRepository {
    suspend fun getAbsences(idAzienda: String, idEmployee: String?, forceRefresh: Boolean = false): Resource<List<Absence>>
    suspend fun deleteOldCachedData(currentDate: LocalDate = LocalDate.now())

}