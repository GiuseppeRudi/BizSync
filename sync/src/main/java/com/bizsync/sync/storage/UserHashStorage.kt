package com.bizsync.sync.storage


import com.bizsync.sync.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    fun saveDipendentiHash(idAzienda: String, hash: String) {
        hashStorage.saveHash("dipendenti_$idAzienda", hash)
    }

    fun getDipendentiHash(idAzienda: String): String? {
        return hashStorage.getHash("dipendenti_$idAzienda")
    }

    fun deleteDipendentiHash(idAzienda: String) {
        hashStorage.deleteHash("dipendenti_$idAzienda")
    }


}