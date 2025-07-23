import com.bizsync.backend.dto.NotaDto
import com.bizsync.domain.model.Nota
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NoteJsonUtils {

//    private val jsonHandler = Json { ignoreUnknownKeys = true }
//
//    fun noteToJson(note: List<Nota>): String {
//        if (note.isEmpty()) return ""
//
//        val noteDto = note.map {
//            NotaDto(
//                id = it.id,
//                testo = it.testo,
//                tipo = it.tipo.name,
//                autore = it.autore,
//                createdAt = it.createdAt.toString(),
//                updatedAt = it.updatedAt.toString()
//            )
//        }
//
//        return jsonHandler.encodeToString(noteDto)
//    }
//
//    fun jsonToNote(json: String): List<Nota> {
//        if (json.isBlank()) return emptyList()
//
//        return try {
//            val noteDto = jsonHandler.decodeFromString<List<NotaDto>>(json)
//
//            noteDto.map {
//                Nota(
//                    id = it.id,
//                    testo = it.testo,
//                    tipo = runCatching { TipoNota.valueOf(it.tipo) }.getOrDefault(TipoNota.GENERALE),
//                    autore = it.autore,
//                    createdAt = it.createdAt.toLocalDate(),
//                    updatedAt = it.updatedAt.toLocalDate()
//                )
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
}
