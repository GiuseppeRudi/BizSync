package com.bizsync.backend.remote


object WeeklyShiftFirestore {

    const val COLLECTION = "weekly_shifts"

    object Fields {
        const val ID = "id"
        const val ID_AZIENDA = "idAzienda"
        const val WEEK_START = "weekStart"
        const val STATUS = "status"
    }

    object Status {
        const val NOT_PUBLISHED = "NOT_PUBLISHED"
        const val PUBLISHED = "PUBLISHED"
        const val COMPLETED = "COMPLETED"
        const val DRAFT = "DRAFT"
    }

    object QueryLimits {
        const val MAX_RESULTS = 100
    }

    // Query paths helpers
    object Paths {
        fun weeklyShiftDocument(weeklyShiftId: String) = "$COLLECTION/$weeklyShiftId"

        // Helper per generare document ID
        fun generateDocumentId(idAzienda: String, weekStart: String): String {
            return "${idAzienda}_$weekStart"
        }
    }

    // Utility per validazione
    object Validation {
        val REQUIRED_FIELDS = listOf(
            Fields.ID_AZIENDA,
            Fields.WEEK_START,
            Fields.STATUS
        )

        val VALID_STATUS = listOf(
            Status.NOT_PUBLISHED,
            Status.PUBLISHED,
            Status.COMPLETED,
            Status.DRAFT
        )
    }
}