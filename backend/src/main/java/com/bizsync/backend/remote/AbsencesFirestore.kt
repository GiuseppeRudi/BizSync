package com.bizsync.backend.remote

object AbsencesFirestore {
    const val COLLECTION = "absences"

    object Fields {
        const val ID = "id"
        const val IDUSER = "idUser"
        const val IDAZIENDA = "idAzienda"
        const val TYPE = "type"
        const val START_DATE_TIME = "startDateTime"
        const val END_DATE_TIME = "endDateTime"
        const val REASON = "reason"
        const val STATUS = "status"
        const val SUBMITTED_DATE = "submittedDate"
        const val APPROVED_BY = "approvedBy"
        const val APPROVED_DATE = "approvedDate"
        const val COMMENTS = "comments"
        const val SUBMITTED_NAME = "submittedName"
        const val TOTAL_HOURS = "totalHours"
        const val TOTAL_DAYS = "totalDays"
    }
}
