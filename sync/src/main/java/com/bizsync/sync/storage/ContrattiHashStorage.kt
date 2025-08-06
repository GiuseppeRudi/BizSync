package com.bizsync.sync.storage


import com.bizsync.sync.HashStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContrattiHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    fun saveContrattiHash(idAzienda: String, hash: String) {
        hashStorage.saveHash("contratti_$idAzienda", hash)
    }

    fun getContrattiHash(idAzienda: String): String? {
        return hashStorage.getHash("contratti_$idAzienda")
    }

    fun deleteContrattiHash(idAzienda: String) {
        hashStorage.deleteHash("contratti_$idAzienda")
    }

}