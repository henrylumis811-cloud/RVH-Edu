package com.henrylumis.rvhgrader.core.repository

import android.content.ContentValues
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.core.model.Learner

class LearnerRepository(private val database: RvhEduDatabase) {
    fun upsert(learner: Learner, schoolId: String = "legacy-school") {
        require(schoolId.isNotBlank()) { "School ID is required" }
        val cleanSchoolId = schoolId.trim()
        val cleanId = learner.id.trim()
        val cleanName = learner.fullName.trim().replace(Regex("\\s+"), " ")
        val cleanAdmission = LearnerDuplicateMatcher.normalizeAdmission(learner.admissionNumber)
        val cleanStatus = learner.status.trim().uppercase(java.util.Locale.ROOT)

        require(cleanId.isNotEmpty()) { "Learner ID is required" }
        require(cleanName.isNotEmpty()) { "Learner name is required" }
        require(learner.ageYears == null || learner.ageYears in 2..30) {
            "Learner age must be between 2 and 30"
        }
        require(cleanStatus == "ACTIVE" || cleanStatus == "ARCHIVED") {
            "Unsupported learner status; use ACTIVE or ARCHIVED"
        }

        val db = database.writableDatabase
        // Keep the normalized duplicate check and write in one transaction. This also catches
        // legacy admission values whose internal whitespace differs from the canonical form.
        db.beginTransaction()
        try {
            if (cleanAdmission != null) {
                val normalized = cleanAdmission.uppercase(java.util.Locale.ROOT)
                val duplicate = db.query(
                    "school_admission_number", arrayOf("learner_id"),
                    "school_id = ? AND normalized_admission_number = ? AND learner_id <> ?",
                    arrayOf(cleanSchoolId, normalized, cleanId), null, null, null
                ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
                require(duplicate == null) {
                    "Admission number $cleanAdmission is already assigned to another learner in this school"
                }
            }

            val values = ContentValues().apply {
                put("id", cleanId)
                // Admission numbers belong to a school, not the shared learner identity.
                putNull("admission_number")
                put("full_name", cleanName)
                learner.ageYears?.let { put("age_years", it) } ?: putNull("age_years")
                put("gender", learner.gender?.trim()?.takeIf { it.isNotEmpty() })
                put("date_of_birth", learner.dateOfBirth?.trim()?.takeIf { it.isNotEmpty() })
                put("photo_uri", learner.photoUri?.trim()?.takeIf { it.isNotEmpty() })
                put("status", "ACTIVE")
                put("created_at", learner.createdAtMillis)
                put("updated_at", learner.updatedAtMillis)
            }
            // Lifecycle status belongs to this school. Never overwrite the shared identity's
            // legacy status when editing or archiving a learner in one school.
            val updatedValues = ContentValues(values).apply { remove("status") }
            val updated = db.update("learner", updatedValues, "id = ?", arrayOf(cleanId))
            if (updated == 0) db.insertOrThrow("learner", null, values)
            val nowStatus = System.currentTimeMillis()
            // Profile edits must not silently change lifecycle state. Seed status only when
            // this school has no lifecycle row yet; archive/restore use setStatusInSchool().
            db.execSQL("""
                INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
                VALUES(?, ?, ?, ?, ?)
            """.trimIndent(), arrayOf(cleanSchoolId, cleanId, cleanStatus, nowStatus, nowStatus))
            db.delete("school_admission_number", "school_id = ? AND learner_id = ?", arrayOf(cleanSchoolId, cleanId))
            if (cleanAdmission != null) {
                val now = System.currentTimeMillis()
                val assignment = ContentValues().apply {
                    put("id", java.util.UUID.randomUUID().toString())
                    put("school_id", cleanSchoolId)
                    put("learner_id", cleanId)
                    put("admission_number", cleanAdmission)
                    put("normalized_admission_number", cleanAdmission.uppercase(java.util.Locale.ROOT))
                    put("created_at", now)
                    put("updated_at", now)
                }
                db.insertOrThrow("school_admission_number", null, assignment)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Change lifecycle state only for the specified school. Caller owns transaction boundaries. */
    fun setStatusInSchool(learnerId: String, schoolId: String, status: String, updatedAtMillis: Long = System.currentTimeMillis()) {
        val cleanSchoolId = schoolId.trim()
        val cleanLearnerId = learnerId.trim()
        val cleanStatus = status.trim().uppercase(java.util.Locale.ROOT)
        require(cleanSchoolId.isNotEmpty()) { "School ID is required" }
        require(cleanLearnerId.isNotEmpty()) { "Learner ID is required" }
        require(cleanStatus == "ACTIVE" || cleanStatus == "ARCHIVED") { "Unsupported learner status" }
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("status", cleanStatus)
            put("updated_at", updatedAtMillis)
        }
        val changed = db.update("school_learner_status", values, "school_id = ? AND learner_id = ?", arrayOf(cleanSchoolId, cleanLearnerId))
        if (changed == 0) {
            val seed = ContentValues().apply {
                put("school_id", cleanSchoolId)
                put("learner_id", cleanLearnerId)
                put("status", cleanStatus)
                put("created_at", updatedAtMillis)
                put("updated_at", updatedAtMillis)
            }
            db.insertOrThrow("school_learner_status", null, seed)
        }
    }

    fun getById(id: String, schoolId: String? = null): Learner? {
        val db = database.readableDatabase
        return if (schoolId.isNullOrBlank()) {
            db.query("learner", null, "id = ?", arrayOf(id), null, null, null)
                .use { c -> if (c.moveToFirst()) c.toLearner() else null }
        } else {
            db.rawQuery(
                "SELECT l.id, COALESCE(san.admission_number, l.admission_number) AS admission_number, l.full_name, l.age_years, l.gender, l.date_of_birth, l.photo_uri, COALESCE(sls.status, l.status) AS status, l.created_at, l.updated_at " +
                    "FROM learner l LEFT JOIN school_admission_number san ON san.learner_id = l.id AND san.school_id = ? LEFT JOIN school_learner_status sls ON sls.learner_id = l.id AND sls.school_id = ? WHERE l.id = ?",
                arrayOf(schoolId.trim(), schoolId.trim(), id)
            ).use { c -> if (c.moveToFirst()) c.toLearner() else null }
        }
    }

    fun getAll(): List<Learner> = database.readableDatabase.query(
        "learner", null, null, null, null, null, "full_name COLLATE NOCASE ASC"
    ).use { c -> buildList { if (c.moveToFirst()) do { add(c.toLearner()) } while (c.moveToNext()) } }

    private fun android.database.Cursor.toLearner() = Learner(
        id = getString(getColumnIndexOrThrow("id")),
        admissionNumber = getStringOrNull("admission_number"),
        fullName = getString(getColumnIndexOrThrow("full_name")),
        ageYears = getIntOrNull("age_years"),
        gender = getStringOrNull("gender"),
        dateOfBirth = getStringOrNull("date_of_birth"),
        photoUri = getStringOrNull("photo_uri"),
        status = getString(getColumnIndexOrThrow("status")),
        createdAtMillis = getLong(getColumnIndexOrThrow("created_at")),
        updatedAtMillis = getLong(getColumnIndexOrThrow("updated_at"))
    )


    /** Compatibility lookup across schools. School-facing flows should use the scoped overload. */
    fun findByAdmissionNumber(admissionNumber: String): Learner? {
        val normalized = LearnerDuplicateMatcher.normalizeAdmission(admissionNumber) ?: return null
        val db = database.readableDatabase
        return db.rawQuery(
            "SELECT l.id, san.school_id FROM school_admission_number san JOIN learner l ON l.id = san.learner_id WHERE san.normalized_admission_number = ? ORDER BY san.school_id LIMIT 1",
            arrayOf(normalized.uppercase(java.util.Locale.ROOT))
        ).use { c -> if (c.moveToFirst()) getById(c.getString(0), c.getString(1)) else null }
    }

    /** School-local admission lookup; same admission number may exist in another school. */
    fun findByAdmissionNumberInSchool(admissionNumber: String, schoolId: String): Learner? {
        val normalized = LearnerDuplicateMatcher.normalizeAdmission(admissionNumber) ?: return null
        require(schoolId.isNotBlank()) { "School ID is required" }
        val db = database.readableDatabase
        return db.rawQuery(
            "SELECT l.id FROM school_admission_number san JOIN learner l ON l.id = san.learner_id WHERE san.school_id = ? AND san.normalized_admission_number = ? LIMIT 1",
            arrayOf(schoolId.trim(), normalized.uppercase(java.util.Locale.ROOT))
        ).use { c -> if (c.moveToFirst()) getById(c.getString(0), schoolId) else null }
    }

    /** Returns the enrolled roster for a school, avoiding unrelated schools' learner rows. */
    fun getEnrolledLearnersForSchool(schoolId: String): List<Learner> {
        require(schoolId.isNotBlank()) { "School ID is required" }
        return database.readableDatabase.rawQuery(
            "SELECT DISTINCT l.id, COALESCE(san.admission_number, l.admission_number) AS admission_number, l.full_name, l.age_years, l.gender, l.date_of_birth, l.photo_uri, COALESCE(sls.status, l.status) AS status, l.created_at, l.updated_at " +
                "FROM learner l INNER JOIN enrollment e ON e.learner_id = l.id " +
                "LEFT JOIN school_admission_number san ON san.learner_id = l.id AND san.school_id = e.school_id " +
                "LEFT JOIN school_learner_status sls ON sls.learner_id = l.id AND sls.school_id = e.school_id " +
                "WHERE e.school_id = ? ORDER BY l.full_name COLLATE NOCASE ASC",
            arrayOf(schoolId.trim())
        ).use { c -> buildList { if (c.moveToFirst()) do { add(c.toLearner()) } while (c.moveToNext()) } }
    }

    /** Find possible matches without assuming that equal names mean the same person. */
    fun findPossibleDuplicates(proposed: Learner, schoolId: String = "legacy-school"): List<PossibleLearnerDuplicate> =
        LearnerDuplicateMatcher.findCandidates(proposed, getEnrolledLearnersForSchool(schoolId))

    /** Name/age lookup uses the same whitespace and Unicode normalization as duplicate review. */
    fun findByNameAndAge(name: String, ageYears: Int?): List<Learner> {
        val normalizedName = LearnerDuplicateMatcher.normalizeName(name)
        return getAll().filter { learner ->
            LearnerDuplicateMatcher.normalizeName(learner.fullName) == normalizedName &&
                (ageYears == null || learner.ageYears == ageYears)
        }
    }

    private fun android.database.Cursor.getIntOrNull(column: String): Int? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getInt(index)
    }

    private fun android.database.Cursor.getStringOrNull(column: String): String? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getString(index)
    }
}
