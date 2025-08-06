package com.bizsync.sync.storage


import com.bizsync.sync.HashStorage
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

}
