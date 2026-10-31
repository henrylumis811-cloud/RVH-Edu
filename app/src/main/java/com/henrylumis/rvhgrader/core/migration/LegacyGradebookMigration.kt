package com.henrylumis.rvhgrader.core.migration

import android.content.ContentValues
import android.content.Context
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.data.GradebookRepository
import com.henrylumis.rvhgrader.model.StudentRecord
import java.util.Calendar

/** Imports the existing gradebook without deleting or modifying the legacy JSON. */
object LegacyGradebookMigration {
    private const val PREFS = "rvh_edu_migration"
    private const val KEY_VERSION = "legacy_gradebook_version"
    private const val CURRENT_VERSION = 2

    fun migrateIfNeeded(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_VERSION, 0) >= CURRENT_VERSION) return 0

        val records = GradebookRepository.load(context)
        if (records.isEmpty()) {
            prefs.edit().putInt(KEY_VERSION, CURRENT_VERSION).apply()
            return 0
        }

        val db = RvhEduDatabase(context)
        var imported = 0
        db.writableDatabase.beginTransaction()
        try {
            val learnerIdsByName = linkedMapOf<String, MutableSet<String>>()

            records.forEach { record ->
                val year = academicYear(record)
                // Legacy data has no reliable learner ID. We therefore scope the generated
                // identity to name + year + class rather than silently merging same-named
                // learners from different class/year contexts.
                val normalizedName = normalizeName(record.name)
                val learnerId = "legacy-learner-${stableKey(normalizedName, year.toString(), record.schoolClass.name)}"
                val enrollmentId = "legacy-enrollment-${stableKey(learnerId, year.toString(), record.schoolClass.name)}"
                val assessmentId = "legacy-assessment-${record.id}"

                upsertLearner(db.writableDatabase, record, learnerId)
                upsertEnrollment(db.writableDatabase, record, learnerId, enrollmentId, year)
                upsertAssessment(db.writableDatabase, record, enrollmentId, assessmentId)

                record.scores.forEach { (subject, score) ->
                    val values = ContentValues().apply {
                        put("id", "legacy-mark-${record.id}-$subject")
                        put("assessment_id", assessmentId)
                        put("subject_code", subject)
                        put("mark", score.mark.coerceAtLeast(0).coerceAtMost(100))
                        put("max_mark", 100)
                        if (score.agg == null) putNull("aggregate") else put("aggregate", score.agg)
                        put("source", "LEGACY_JSON")
                        put("updated_at", record.recordedAtMillis)
                    }
                    db.writableDatabase.insertWithOnConflict("mark", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                }

                learnerIdsByName.getOrPut(normalizedName) { linkedSetOf() }.add(learnerId)
                imported++
            }

            // Same names appearing in different generated identities are ambiguous. Keep the
            // data separate and create a review item instead of silently merging learners.
            learnerIdsByName.forEach { (name, ids) ->
                val uniqueIds = ids.toList()
                if (uniqueIds.size > 1) {
                    for (i in uniqueIds.indices) {
                        for (j in i + 1 until uniqueIds.size) {
                            addIdentityReview(db.writableDatabase, name, uniqueIds[i], uniqueIds[j])
                        }
                    }
                }
            }

            db.writableDatabase.setTransactionSuccessful()
        } finally {
            db.writableDatabase.endTransaction()
            db.close()
        }

        prefs.edit().putInt(KEY_VERSION, CURRENT_VERSION).apply()
        return imported
    }

    private fun upsertLearner(db: android.database.sqlite.SQLiteDatabase, r: StudentRecord, id: String) {
        val values = ContentValues().apply {
            put("id", id)
            putNull("admission_number")
            put("full_name", r.name.trim())
            put("status", "ACTIVE")
            put("created_at", r.recordedAtMillis)
            put("updated_at", r.recordedAtMillis)
        }
        db.insertWithOnConflict("learner", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun upsertEnrollment(
        db: android.database.sqlite.SQLiteDatabase,
        r: StudentRecord,
        learnerId: String,
        id: String,
        year: Int
    ) {
        val values = ContentValues().apply {
            put("id", id)
            put("learner_id", learnerId)
            put("academic_year", year)
            put("school_class", r.schoolClass.name)
            putNull("stream")
            put("enrolled_at", r.recordedAtMillis)
        }
        db.insertWithOnConflict("enrollment", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun upsertAssessment(db: android.database.sqlite.SQLiteDatabase, r: StudentRecord, enrollmentId: String, id: String) {
        val values = ContentValues().apply {
            put("id", id)
            put("enrollment_id", enrollmentId)
            put("term", r.term.name)
            put("section", r.section.name)
            put("recorded_at", r.recordedAtMillis)
        }
        // Never use SQLite REPLACE here: REPLACE deletes the conflicting row first,
        // which would cascade-delete its marks. Migration must be non-destructive.
        val updated = db.update("assessment", values, "id = ?", arrayOf(id))
        if (updated == 0) {
            try {
                db.insertOrThrow("assessment", null, values)
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                // A unique enrollment/term/section collision already exists. Preserve the
                // existing assessment and its marks rather than replacing it.
                if (db.query("assessment", arrayOf("id"),
                        "enrollment_id = ? AND term = ? AND section = ?",
                        arrayOf(enrollmentId, r.term.name, r.section.name), null, null, null)
                        .use { it.moveToFirst() }) return
                throw e
            }
        }
    }

    private fun addIdentityReview(db: android.database.sqlite.SQLiteDatabase, name: String, a: String, b: String) {
        val values = ContentValues().apply {
            put("id", "legacy-review-${stableKey(a, b)}")
            put("normalized_name", name)
            put("learner_id", a)
            put("related_learner_id", b)
            put("reason", "Same legacy name appears in multiple class/year identities; teacher review required")
            put("resolved", 0)
            put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("legacy_identity_review", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun academicYear(record: StudentRecord): Int =
        Calendar.getInstance().apply { timeInMillis = record.recordedAtMillis }.get(Calendar.YEAR)

    private fun normalizeName(name: String): String = name.trim().uppercase().replace(Regex("\\s+"), " ")

    private fun stableKey(vararg values: String): String = values.joinToString("|").hashCode().toUInt().toString(16)
}
