package com.bizsync.backend.hash.storage


import com.bizsync.backend.hash.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurniHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    fun saveTurniHash(idAzienda: String, hash: String) {
        hashStorage.saveHash("turni_$idAzienda", hash)
    }

    fun getTurniHash(idAzienda: String): String? {
        return hashStorage.getHash("turni_$idAzienda")
    }

    fun deleteTurniHash(idAzienda: String) {
        hashStorage.deleteHash("turni_$idAzienda")
    }


    fun deleteLastSyncTime(idAzienda: String) {
        hashStorage.deleteHash("turni_last_sync_$idAzienda")
    }


}
