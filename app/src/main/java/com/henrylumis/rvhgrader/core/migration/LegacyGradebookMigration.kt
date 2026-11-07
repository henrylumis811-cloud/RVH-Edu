package com.henrylumis.rvhgrader.core.migration

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.data.GradebookRepository
import com.henrylumis.rvhgrader.model.StudentRecord
import java.io.File
import java.io.IOException
import java.util.Calendar

/** Imports the legacy gradebook without modifying or deleting its source file. */
object LegacyGradebookMigration {
    private const val TAG = "RVHEduMigration"
    private const val PREFS = "rvh_edu_migration"
    private const val KEY_VERSION = "legacy_gradebook_version"
    // Bump to retry migrations previously marked complete despite unreadable legacy JSON.
    private const val CURRENT_VERSION = 3

    fun migrateIfNeeded(context: Context): Int {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_VERSION, 0) >= CURRENT_VERSION) return 0

        val startedAt = System.currentTimeMillis()
        var phase = "migration_started"
        logPhase(phase, startedAt)
        val legacyFile = File(appContext.filesDir, "gradebook.json")

        // Back up the original bytes before attempting to parse or import anything. Never
        // overwrite a previous pre-migration snapshot, and never change gradebook.json here.
        if (legacyFile.exists()) {
            phase = "backup_legacy_json"
            val backup = createBackup(legacyFile, appContext.filesDir)
            Log.i(TAG, "phase=$phase backup=${backup.name} bytes=${backup.length()}")
        }

        phase = "legacy_loaded"
        // load() throws on malformed/unreadable data. That failure must not be mistaken for
        // an empty gradebook, and the migration marker must remain untouched so retry is safe.
        val records = try {
            GradebookRepository.load(appContext)
        } catch (e: Exception) {
            Log.e(TAG, "phase=$phase failed; migration marker not advanced", e)
            throw IllegalStateException("Legacy gradebook could not be read. A backup was preserved; no migration was committed.", e)
        }
        Log.i(TAG, "phase=$phase count=${records.size} elapsedMs=${System.currentTimeMillis() - startedAt}")

        // A genuinely missing or empty legacy file is a valid no-op. A present non-empty file
        // has already been parsed successfully before this point.
        if (records.isEmpty()) {
            prefs.edit().putInt(KEY_VERSION, CURRENT_VERSION).commitOrThrow("empty gradebook migration marker")
            logPhase("migration_complete_empty", startedAt)
            return 0
        }

        var helper: RvhEduDatabase? = null
        var db: SQLiteDatabase? = null
        var transactionStarted = false
        var imported = 0
        try {
            phase = "database_opened"
            val databaseHelper = RvhEduDatabase(appContext)
            helper = databaseHelper
            val database = databaseHelper.writableDatabase
            db = database
            logPhase(phase, startedAt)

            phase = "transaction_started"
            database.beginTransaction()
            transactionStarted = true
            logPhase(phase, startedAt)

            val learnerIdsByName = linkedMapOf<String, MutableSet<String>>()
            records.forEachIndexed { index, record ->
                val year = academicYear(record)
                val normalizedName = normalizeName(record.name)
                val learnerId = "legacy-learner-${stableKey(normalizedName, year.toString(), record.schoolClass.name)}"
                val enrollmentId = "legacy-enrollment-${stableKey(learnerId, year.toString(), record.schoolClass.name)}"
                val proposedAssessmentId = "legacy-assessment-${record.id}"

                upsertLearner(database, record, learnerId)
                upsertEnrollment(database, record, learnerId, enrollmentId, year)
                // Unique enrollment/term/section collisions must return the canonical existing
                // assessment ID. Marks are always attached to the row that actually exists.
                val assessmentId = upsertAssessment(database, record, enrollmentId, proposedAssessmentId)

                record.scores.forEach { (subject, score) ->
                    val values = ContentValues().apply {
                        put("id", "legacy-mark-${record.id}-$subject")
                        put("assessment_id", assessmentId)
                        put("subject_code", subject)
                        put("mark", score.mark.coerceIn(0, 100))
                        put("max_mark", 100)
                        if (score.agg == null) putNull("aggregate") else put("aggregate", score.agg)
                        put("source", "LEGACY_JSON")
                        put("updated_at", record.recordedAtMillis)
                    }
                    database.insertWithOnConflict("mark", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
                learnerIdsByName.getOrPut(normalizedName) { linkedSetOf() }.add(learnerId)
                imported++
                if ((index + 1) % 100 == 0) {
                    Log.i(TAG, "phase=import_progress imported=${index + 1}/${records.size} elapsedMs=${System.currentTimeMillis() - startedAt}")
                }
            }

            phase = "identity_review"
            learnerIdsByName.forEach { (name, ids) ->
                val uniqueIds = ids.toList()
                for (i in uniqueIds.indices) for (j in i + 1 until uniqueIds.size) {
                    addIdentityReview(database, name, uniqueIds[i], uniqueIds[j])
                }
            }

            phase = "transaction_committing"
            database.setTransactionSuccessful()
            logPhase(phase, startedAt)
        } catch (e: Exception) {
            Log.e(TAG, "phase=$phase failed; transaction will roll back", e)
            throw IllegalStateException("Legacy gradebook migration failed during '$phase'. Your original gradebook was preserved. You can retry safely.", e)
        } finally {
            // Only end a transaction that actually began. Nested finally guarantees the helper
            // is closed even if endTransaction() fails; cleanup failures must propagate so the
            // migration-complete marker is never advanced after an uncertain commit.
            try {
                if (transactionStarted) db?.endTransaction()
            } finally {
                helper?.close()
            }
        }

        phase = "migration_marker_commit"
        prefs.edit().putInt(KEY_VERSION, CURRENT_VERSION).commitOrThrow("migration completion marker")
        Log.i(TAG, "phase=transaction_committed imported=$imported total=${records.size} elapsedMs=${System.currentTimeMillis() - startedAt}")
        logPhase("migration_complete", startedAt)
        return imported
    }

    private fun createBackup(source: File, filesDir: File): File {
        val backupDir = File(filesDir, "migration-backups")
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw IOException("Could not create the migration backup directory; no migration was attempted.")
        }
        val base = "gradebook-pre-migration-v3.json"
        var destination = File(backupDir, base)
        var suffix = 1
        while (destination.exists()) destination = File(backupDir, "gradebook-pre-migration-v3-$suffix.json").also { suffix++ }
        source.inputStream().use { input -> destination.outputStream().use { output -> input.copyTo(output) } }
        if (destination.length() != source.length()) {
            destination.delete()
            throw IOException("The gradebook backup could not be verified; migration was stopped.")
        }
        return destination
    }

    private fun logPhase(phase: String, startedAt: Long) {
        Log.i(TAG, "phase=$phase elapsedMs=${System.currentTimeMillis() - startedAt}")
    }

    private fun android.content.SharedPreferences.Editor.commitOrThrow(label: String) {
        if (!commit()) throw IllegalStateException("Could not save $label. Migration can be safely retried.")
    }

    private fun upsertLearner(db: SQLiteDatabase, r: StudentRecord, id: String) {
        val values = ContentValues().apply {
            put("id", id); putNull("admission_number"); put("full_name", r.name.trim())
            put("status", "ACTIVE"); put("created_at", r.recordedAtMillis); put("updated_at", r.recordedAtMillis)
        }
        db.insertWithOnConflict("learner", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun upsertEnrollment(db: SQLiteDatabase, r: StudentRecord, learnerId: String, id: String, year: Int) {
        val values = ContentValues().apply {
            put("id", id); put("learner_id", learnerId); put("academic_year", year)
            put("school_class", r.schoolClass.name); putNull("stream"); put("enrolled_at", r.recordedAtMillis)
        }
        db.insertWithOnConflict("enrollment", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun upsertAssessment(db: SQLiteDatabase, r: StudentRecord, enrollmentId: String, id: String): String {
        val values = ContentValues().apply {
            put("id", id); put("enrollment_id", enrollmentId); put("term", r.term.name)
            put("section", r.section.name); put("recorded_at", r.recordedAtMillis)
        }
        db.update("assessment", values, "id = ?", arrayOf(id))
        val byId = findAssessmentId(db, "id = ?", arrayOf(id))
        if (byId != null) return byId
        try {
            db.insertOrThrow("assessment", null, values)
            return id
        } catch (e: SQLiteConstraintException) {
            val canonical = findAssessmentId(db, "enrollment_id = ? AND term = ? AND section = ?",
                arrayOf(enrollmentId, r.term.name, r.section.name))
            if (canonical != null) return canonical
            throw e
        }
    }

    private fun findAssessmentId(db: SQLiteDatabase, where: String, args: Array<String>): String? =
        db.query("assessment", arrayOf("id"), where, args, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private fun addIdentityReview(db: SQLiteDatabase, name: String, a: String, b: String) {
        val values = ContentValues().apply {
            put("id", "legacy-review-${stableKey(a, b)}"); put("normalized_name", name)
            put("learner_id", a); put("related_learner_id", b)
            put("reason", "Same legacy name appears in multiple class/year identities; teacher review required")
            put("resolved", 0); put("created_at", System.currentTimeMillis())
        }
        db.insertWithOnConflict("legacy_identity_review", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun academicYear(record: StudentRecord): Int =
        Calendar.getInstance().apply { timeInMillis = record.recordedAtMillis }.get(Calendar.YEAR)

    private fun normalizeName(name: String): String = name.trim().uppercase().replace(Regex("\\s+"), " ")
    private fun stableKey(vararg values: String): String = values.joinToString("|").hashCode().toUInt().toString(16)
}
