package com.bizsync.backend.hash.storage

// 3. ContrattiHashStorage.kt

import com.bizsync.backend.hash.HashStorage
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

    // Metodi aggiuntivi utili per i contratti
    fun saveLastSyncTime(idAzienda: String, timestamp: Long) {
        hashStorage.saveHash("contratti_last_sync_$idAzienda", timestamp.toString())
    }

    fun getLastSyncTime(idAzienda: String): Long? {
        return hashStorage.getHash("contratti_last_sync_$idAzienda")?.toLongOrNull()
    }

    fun deleteLastSyncTime(idAzienda: String) {
        hashStorage.deleteHash("contratti_last_sync_$idAzienda")
    }

    // Salva hash di un singolo contratto (per update specifici)
    fun saveSingleContractHash(contractId: String, hash: String) {
        hashStorage.saveHash("contract_single_$contractId", hash)
    }

    fun getSingleContractHash(contractId: String): String? {
        return hashStorage.getHash("contract_single_$contractId")
    }

    fun deleteSingleContractHash(contractId: String) {
        hashStorage.deleteHash("contract_single_$contractId")
    }

    // Pulisce tutta la cache per i contratti di un'azienda
    fun clearContrattiCache(idAzienda: String) {
        deleteContrattiHash(idAzienda)
        deleteLastSyncTime(idAzienda)
    }
}