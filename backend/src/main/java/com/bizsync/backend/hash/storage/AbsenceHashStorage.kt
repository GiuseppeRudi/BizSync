package com.bizsync.backend.hash.storage

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.bizsync.backend.hash.HashStorage


@Singleton
class AbsenceHashStorage @Inject constructor(
    private val hashStorage: HashStorage
) {

    // Salva l'hash delle assenze per una settimana specifica
    fun saveAbsenceHash(idAzienda: String, weekStart: String, hash: String) {
        hashStorage.saveHash("absence_hash_${idAzienda}_$weekStart", hash)
    }

    fun getAbsenceHash(idAzienda: String, weekStart: String): String? {
        return hashStorage.getHash("absence_hash_${idAzienda}_$weekStart")
    }

    fun deleteAbsenceCache(idAzienda: String) {
        val prefix = "absence_hash_${idAzienda}_"
        val allKeys = hashStorage.getAllKeys()

        allKeys.filter { it.startsWith(prefix) }
            .forEach { hashStorage.deleteHash(it) }
    }



}