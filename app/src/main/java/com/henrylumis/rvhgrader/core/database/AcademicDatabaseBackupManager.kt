package com.henrylumis.rvhgrader.core.database

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject

/** Lightweight, read-only summary shown before the user confirms a restore. */
data class BackupPreview(
    val backupVersion: Int,
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val learnerCount: Int,
    val enrollmentCount: Int,
    val assessmentCount: Int,
    val markCount: Int,
    val checksumVerified: Boolean
)

/** Portable, transaction-safe backup for the RVH Edu SQLite academic store. */
object AcademicDatabaseBackupManager {
    private const val BACKUP_TYPE = "RVH_EDU_DATABASE_BACKUP"
    private const val BACKUP_VERSION = 2
    private const val SCHEMA_VERSION = 10
    private const val MAX_BACKUP_ROWS = 250_000
    // Bound both file reads and JSON parsing to avoid exhausting memory on a damaged backup.
    private const val MAX_BACKUP_BYTES = 64L * 1024L * 1024L
    private const val BLOB_MARKER = "__rvhEduBlobBase64"
    private val tables = listOf("school", "teacher", "teacher_school_membership", "teaching_assignment", "learner", "school_admission_number", "enrollment", "assessment", "mark", "legacy_identity_review", "learner_status_history", "school_learner_status")

    fun buildJson(context: Context): String {
        val helper = RvhEduDatabase(context)
        try {
            val db = helper.writableDatabase
            AcademicDatabaseIntegrity.requireHealthy(db)
            try { db.execSQL("PRAGMA wal_checkpoint(FULL)") } catch (_: Exception) { /* checkpoint is best-effort */ }
            val tableObject = JSONObject().also { objectValue ->
                tables.forEach { objectValue.put(it, dumpTable(db, it)) }
            }
            val root = JSONObject().apply {
                put("backupType", BACKUP_TYPE)
                put("backupVersion", BACKUP_VERSION)
                put("schemaVersion", SCHEMA_VERSION)
                put("exportedAtMillis", System.currentTimeMillis())
                put("tables", tableObject)
                put("contentSha256", sha256(canonicalJson(tableObject)))
            }
            val payload = root.toString(2)
            require(payload.toByteArray(Charsets.UTF_8).size.toLong() <= MAX_BACKUP_BYTES) {
                "The backup is too large to export safely. Reduce the amount of stored data or export from a newer device."
            }
            return payload
        } finally {
            helper.close()
        }
    }

    fun writeTo(context: Context, uri: Uri) {
        // Build and validate the entire payload before opening the destination. This avoids
        // truncating a previously selected file if backup generation itself fails.
        val bytes = buildJson(context).toByteArray(Charsets.UTF_8)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(bytes)
            out.flush()
        } ?: throw IllegalStateException("Could not open the selected destination.")
    }

    /** Read a bounded backup file without opening or changing the academic database. */
    private fun readBackupBytes(context: Context, uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= MAX_BACKUP_BYTES) {
                    "This backup is larger than RVH Edu's safe import limit (64 MB). The database has not been changed."
                }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: throw IllegalStateException("Could not read the selected backup.")

    /**
     * Read-only preview for the confirmation dialog. This verifies the file envelope and v2
     * checksum, but full domain/schema validation still runs during restore before any rows are deleted.
     */
    fun previewFrom(context: Context, uri: Uri): BackupPreview {
        val root = JSONObject(readBackupBytes(context, uri).toString(Charsets.UTF_8))
        require(root.optString("backupType") == BACKUP_TYPE) { "This is not an RVH Edu database backup." }
        val version = root.optInt("backupVersion", -1)
        require(version == 1 || version == BACKUP_VERSION) { "Unsupported RVH Edu backup version." }
        val schema = root.optInt("schemaVersion")
        require(schema in 4..SCHEMA_VERSION) { "Unsupported database schema in this backup." }
        val exportedAt = root.optLong("exportedAtMillis", 0L)
        require(exportedAt > 0L) { "Backup timestamp is missing or invalid." }
        val tableObject = root.optJSONObject("tables") ?: error("Backup contains no database tables.")
        if (version >= 2) {
            val expected = root.optString("contentSha256", "")
            require(expected.matches(Regex("[0-9a-f]{64}"))) { "Backup integrity checksum is missing or malformed." }
            require(expected == sha256(canonicalJson(tableObject))) {
                "Backup integrity check failed. The file may be damaged or modified."
            }
        }
        normalizeLegacyBackupTables(tableObject, schema)
        require(tableObject.length() == tables.size) { "Backup contains missing or unknown database tables." }
        tables.forEach { table -> require(tableObject.optJSONArray(table) != null) { "Backup is missing a valid table: $table" } }
        fun count(table: String): Int = tableObject.optJSONArray(table)?.length() ?: 0
        return BackupPreview(version, schema, exportedAt, count("learner"), count("enrollment"), count("assessment"), count("mark"), version >= 2)
    }

    fun restoreFrom(context: Context, uri: Uri) {
        restoreJson(context, readBackupBytes(context, uri).toString(Charsets.UTF_8))
    }

    fun restoreJson(context: Context, json: String) {
        require(json.toByteArray(Charsets.UTF_8).size.toLong() <= MAX_BACKUP_BYTES) {
            "This backup is larger than RVH Edu's safe import limit (64 MB). The database has not been changed."
        }
        val root = JSONObject(json)
        require(root.optString("backupType") == BACKUP_TYPE) { "This is not an RVH Edu database backup." }
        val backupVersion = root.optInt("backupVersion", -1)
        require(backupVersion == 1 || backupVersion == BACKUP_VERSION) { "Unsupported RVH Edu backup version." }
        val backupSchemaVersion = root.optInt("schemaVersion")
        require(backupSchemaVersion in 4..SCHEMA_VERSION) {
            "Unsupported database schema in this backup. Supported schemas: 4 and $SCHEMA_VERSION."
        }
        require(root.optLong("exportedAtMillis", 0L) > 0L) { "Backup timestamp is missing or invalid." }
        val tableObject = root.optJSONObject("tables") ?: error("Backup contains no database tables.")
        // Version 2 backups carry a SHA-256 digest of canonicalized table data. Version 1
        // backups remain importable for compatibility, but cannot claim checksum verification.
        if (backupVersion >= 2) {
            val expectedDigest = root.optString("contentSha256", "")
            require(expectedDigest.matches(Regex("[0-9a-f]{64}"))) {
                "Backup integrity checksum is missing or malformed. The existing database has not been changed."
            }
            val actualDigest = sha256(canonicalJson(tableObject))
            require(expectedDigest == actualDigest) {
                "Backup integrity check failed. The file may be damaged or modified; the existing database has not been changed."
            }
        }
        // Normalize known older backup shapes only after validating the original checksum.
        // This preserves checksum semantics while allowing legacy rows to use current defaults.
        normalizeLegacyBackupTables(tableObject, backupSchemaVersion)
        tables.forEach { table ->
            require(tableObject.has(table) && tableObject.optJSONArray(table) != null) {
                "Backup is missing a valid table: $table"
            }
        }
        require(tableObject.length() == tables.size) { "Backup contains unknown database tables." }
        require(tables.all { tableObject.has(it) }) { "Backup is missing required database tables." }

        // Reject semantically impossible records before opening a destructive restore transaction.
        // SQLite constraints remain the final authority, but these checks produce clearer errors
        // and keep malformed academic data from reaching the delete/insert phase.
        validateDomainRows(tableObject, backupSchemaVersion)

        val helper = RvhEduDatabase(context)
        try {
            val db = helper.writableDatabase
            validateBackupRows(db, tableObject, backupSchemaVersion)
            db.beginTransaction()
            try {
                // Children first because foreign keys are enabled.
                listOf("teaching_assignment", "teacher_school_membership", "school_learner_status", "learner_status_history", "legacy_identity_review", "mark", "assessment", "enrollment", "school_admission_number", "learner", "teacher", "school").forEach {
                    db.delete(it, null, null)
                }
                tables.forEach { table -> restoreTable(db, table, tableObject.getJSONArray(table)) }
                // Older backups stored admission numbers on the shared learner row. Import them
                // into school-local assignments, then clear the legacy column in the same transaction.
                migrateLegacyAdmissionsInRestoredBackup(db)
                // Only pre-v10 backups need status-row synthesis. A current-schema backup
                // must already contain a lifecycle status for every enrolled school/learner
                // pair; otherwise preflight rejects it instead of silently repairing a
                // potentially incomplete backup during restore.
                if (backupSchemaVersion < SCHEMA_VERSION) backfillSchoolStatusesAfterRestore(db)
                AcademicDatabaseIntegrity.requireHealthy(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } finally {
            helper.close()
        }
    }


    /** Convert Android JSON containers to a pure JSON-shaped tree and use the tested serializer. */
    private fun canonicalJson(value: Any): String = BackupChecksumPrimitives.canonicalJson(toChecksumTree(value))

    private fun toChecksumTree(value: Any): Any? = when (value) {
        is JSONObject -> {
            val result = linkedMapOf<String, Any?>()
            value.keys().asSequence().toList().sorted().forEach { key ->
                result[key] = toChecksumTree(value.get(key))
            }
            result
        }
        is JSONArray -> (0 until value.length()).map { index -> toChecksumTree(value.get(index)) }
        JSONObject.NULL -> null
        is String, is Boolean, is Number -> value
        else -> error("Unsupported value in backup checksum.")
    }

    private fun canonicalNumber(value: Number): String = BackupChecksumPrimitives.canonicalNumber(value)

    private fun sha256(value: String): String = BackupChecksumPrimitives.sha256(value)

    private fun validateDomainRows(tableObject: JSONObject, backupSchemaVersion: Int) {
        val learners = tableObject.getJSONArray("learner")
        val learnerIds = mutableSetOf<String>()
        for (i in 0 until learners.length()) {
            val row = learners.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && learnerIds.add(id)) { "Backup contains a blank or duplicate learner ID." }
            require(row.optString("full_name").trim().isNotEmpty()) { "Backup contains a learner without a name." }
            require(row.optString("status") in setOf("ACTIVE", "ARCHIVED")) {
                "Backup contains an unsupported learner status for learner $id. The existing database has not been changed."
            }
            if (!row.isNull("age_years") && row.has("age_years")) {
                val age = row.optInt("age_years", -1)
                require(age in 2..30) { "Backup contains an invalid age for learner $id." }
            }
        }

        val schools = tableObject.getJSONArray("school")
        val schoolIds = mutableSetOf<String>()
        for (i in 0 until schools.length()) {
            val row = schools.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && schoolIds.add(id)) { "Backup contains a blank or duplicate school ID." }
        }
        val admissionRows = tableObject.getJSONArray("school_admission_number")
        val admissionLearnerKeys = mutableSetOf<String>()
        for (i in 0 until admissionRows.length()) {
            val row = admissionRows.getJSONObject(i)
            val schoolId = row.optString("school_id").trim()
            val learnerId = row.optString("learner_id").trim()
            val admission = row.optString("admission_number").trim()
            val normalized = row.optString("normalized_admission_number").trim()
            require(schoolId in schoolIds && learnerId in learnerIds) {
                "Backup contains a school-local admission assignment for a missing school or learner."
            }
            require(admission.isNotEmpty() && normalized.isNotEmpty()) {
                "Backup contains an empty school-local admission number."
            }
            require(admissionLearnerKeys.add("$schoolId\u001f$learnerId")) {
                "Backup contains more than one admission assignment for the same learner in one school."
            }
            require(normalized == admission.uppercase(java.util.Locale.ROOT)) {
                "Backup contains a non-normalized school-local admission number."
            }
        }

        val enrollments = tableObject.getJSONArray("enrollment")
        val enrollmentIds = mutableSetOf<String>()
        val enrollmentKeys = mutableSetOf<String>()
        for (i in 0 until enrollments.length()) {
            val row = enrollments.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && enrollmentIds.add(id)) { "Backup contains a blank or duplicate enrolment ID." }
            require(row.optString("learner_id") in learnerIds) { "Backup contains an enrolment for a missing learner." }
            require(row.optString("school_id").trim() in schoolIds) {
                "Backup contains an enrolment referencing a missing school. The existing database has not been changed."
            }
            require(row.optInt("academic_year", 0) in 1900..2200) { "Backup contains an invalid academic year." }
            val schoolClass = row.optString("school_class")
            require(schoolClass in setOf("P1", "P2", "P3", "P4", "P5", "P6", "P7")) {
                "Backup contains an unsupported school class in an enrolment."
            }
            val enrollmentKey = listOf(row.optString("school_id"), row.optString("learner_id"), row.optInt("academic_year"), schoolClass).joinToString("\u001f")
            require(enrollmentKeys.add(enrollmentKey)) {
                "Backup contains duplicate enrolments for the same learner, year and class."
            }
        }

        val assessments = tableObject.getJSONArray("assessment")
        val assessmentIds = mutableSetOf<String>()
        val assessmentKeys = mutableSetOf<String>()
        for (i in 0 until assessments.length()) {
            val row = assessments.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && assessmentIds.add(id)) { "Backup contains a blank or duplicate assessment ID." }
            require(row.optString("enrollment_id") in enrollmentIds) { "Backup contains an assessment for a missing enrolment." }
            require(row.optString("term") in setOf("TERM1", "TERM2", "TERM3")) { "Backup contains an unsupported term." }
            require(row.optString("section") in setOf("BEG", "MID", "END")) { "Backup contains an unsupported assessment section." }
            require(row.optString("status") in setOf("DRAFT", "FINAL")) { "Backup contains an unsupported assessment status." }
            val assessmentKey = listOf(row.optString("enrollment_id"), row.optString("term"), row.optString("section")).joinToString("\u001f")
            require(assessmentKeys.add(assessmentKey)) {
                "Backup contains duplicate assessments for the same enrolment, term and section."
            }
        }

        val marks = tableObject.getJSONArray("mark")
        val markIds = mutableSetOf<String>()
        val markKeys = mutableSetOf<String>()
        for (i in 0 until marks.length()) {
            val row = marks.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && markIds.add(id)) { "Backup contains a blank or duplicate mark ID." }
            require(row.optString("assessment_id") in assessmentIds) { "Backup contains a mark for a missing assessment." }
            val subjectCode = row.optString("subject_code").trim()
            require(subjectCode.isNotEmpty()) { "Backup contains a mark without a subject code." }
            val markKey = listOf(row.optString("assessment_id"), subjectCode).joinToString("\u001f")
            require(markKeys.add(markKey)) { "Backup contains duplicate marks for the same assessment and subject." }
            val mark = row.optInt("mark", -1)
            val maxMark = row.optInt("max_mark", 0)
            require(maxMark > 0 && mark in 0..maxMark) { "Backup contains an invalid mark value." }
        }

        val identityReviews = tableObject.getJSONArray("legacy_identity_review")
        val identityReviewIds = mutableSetOf<String>()
        val identityReviewKeys = mutableSetOf<String>()
        for (i in 0 until identityReviews.length()) {
            val row = identityReviews.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && identityReviewIds.add(id)) { "Backup contains a blank or duplicate identity-review ID." }
            val learnerId = row.optString("learner_id")
            val relatedLearnerId = row.optString("related_learner_id")
            require(learnerId in learnerIds && relatedLearnerId in learnerIds) {
                "Backup contains an identity review referencing a missing learner."
            }
            require(learnerId != relatedLearnerId) { "Backup contains an identity review linking a learner to themselves." }
            require(row.optString("normalized_name").trim().isNotEmpty()) { "Backup contains an identity review without a normalized name." }
            require(row.optString("reason").trim().isNotEmpty()) { "Backup contains an identity review without a reason." }
            val resolved = row.optInt("resolved", 0)
            require(resolved == 0 || resolved == 1) { "Backup contains an invalid identity-review resolution status." }
            require(row.optLong("created_at", 0L) > 0L) { "Backup contains an invalid identity-review timestamp." }
            val key = listOf(learnerId, relatedLearnerId, row.optString("reason")).joinToString("\u001f")
            require(identityReviewKeys.add(key)) { "Backup contains duplicate identity-review entries." }
        }

        val schoolStatuses = tableObject.getJSONArray("school_learner_status")
        val schoolStatusKeys = mutableSetOf<String>()
        for (i in 0 until schoolStatuses.length()) {
            val row = schoolStatuses.getJSONObject(i)
            val schoolId = row.optString("school_id").trim()
            val learnerId = row.optString("learner_id").trim()
            require(schoolId in schoolIds && learnerId in learnerIds) {
                "Backup contains a school-local learner status referencing a missing school or learner. The existing database has not been changed."
            }
            require(row.optString("status") in setOf("ACTIVE", "ARCHIVED")) {
                "Backup contains an unsupported school-local learner status. The existing database has not been changed."
            }
            val createdAt = row.optLong("created_at", 0L)
            val updatedAt = row.optLong("updated_at", 0L)
            require(createdAt > 0L && updatedAt > 0L) {
                "Backup contains an invalid school-local learner-status timestamp. The existing database has not been changed."
            }
            // Reject timestamp order errors in preflight, before the restore transaction starts.
            // The post-restore integrity audit remains a second line of defence.
            require(updatedAt >= createdAt) {
                "Backup contains a learner-status update timestamp earlier than its creation timestamp. The existing database has not been changed."
            }
            require(schoolStatusKeys.add("$schoolId\u001f$learnerId")) {
                "Backup contains duplicate school-local learner statuses. The existing database has not been changed."
            }
        }
        if (backupSchemaVersion >= SCHEMA_VERSION) {
            // Schema-v10 exports always include the school-local lifecycle table. Every
            // enrollment must have its matching status row; don't let restore backfill a
            // missing row from the legacy global learner.status field, which could erase
            // the distinction between schools.
            for (i in 0 until enrollments.length()) {
                val row = enrollments.getJSONObject(i)
                val learnerId = row.optString("learner_id")
                val key = "${row.optString("school_id")}\u001f$learnerId"
                require(key in schoolStatusKeys) {
                    "Current-schema backup is missing a learner-status record for an enrolled learner. The existing database has not been changed."
                }
            }
            // A learner may be created in the currently selected school before their
            // first enrollment is recorded. Such a learner's status may belong to that
            // school rather than legacy-school. Require at least one status assignment,
            // but do not invent an enrollment or force a false legacy-school attribution.
            for (learnerId in learnerIds) {
                require(schoolStatusKeys.any { it.endsWith("\u001f$learnerId") }) {
                    "Current-schema backup is missing every lifecycle status for learner $learnerId. The existing database has not been changed."
                }
            }
        }

        val history = tableObject.getJSONArray("learner_status_history")
        val historyIds = mutableSetOf<String>()
        for (i in 0 until history.length()) {
            val row = history.getJSONObject(i)
            val id = row.optString("id").trim()
            require(id.isNotEmpty() && historyIds.add(id)) { "Backup contains a blank or duplicate learner status-history ID." }
            require(row.optString("learner_id") in learnerIds) { "Backup contains status history for a missing learner." }
            require(row.optString("school_id").trim() in schoolIds) {
                "Backup contains learner status history referencing a missing school. The existing database has not been changed."
            }
            val fromStatus = row.optString("from_status")
            val toStatus = row.optString("to_status")
            require(fromStatus in setOf("ACTIVE", "ARCHIVED") && toStatus in setOf("ACTIVE", "ARCHIVED") && fromStatus != toStatus) {
                "Backup contains an unsupported learner status-history transition."
            }
            require(row.optString("reason").trim().isNotEmpty()) { "Backup contains a learner status-history entry without a reason." }
            require(row.optLong("changed_at", 0L) > 0L) { "Backup contains an invalid learner status-history timestamp." }
        }
    }

    private fun dumpTable(db: SQLiteDatabase, table: String): JSONArray {
        val array = JSONArray()
        db.query(table, null, null, null, null, null, null).use { cursor ->
            val columns = cursor.columnNames
            while (cursor.moveToNext()) {
                val row = JSONObject()
                columns.forEachIndexed { index, column ->
                    if (cursor.isNull(index)) row.put(column, JSONObject.NULL)
                    else when (cursor.getType(index)) {
                        android.database.Cursor.FIELD_TYPE_INTEGER -> row.put(column, cursor.getLong(index))
                        android.database.Cursor.FIELD_TYPE_FLOAT -> row.put(column, cursor.getDouble(index))
                        android.database.Cursor.FIELD_TYPE_BLOB -> row.put(
                            column,
                            JSONObject().put(BLOB_MARKER, android.util.Base64.encodeToString(cursor.getBlob(index), android.util.Base64.NO_WRAP))
                        )
                        else -> row.put(column, cursor.getString(index))
                    }
                }
                array.put(row)
            }
        }
        return array
    }

    /** Validate the complete JSON shape against the installed schema before deleting any rows. */
    private fun validateBackupRows(db: SQLiteDatabase, tableObject: JSONObject, backupSchemaVersion: Int) {
        var totalRows = 0
        tables.forEach { table ->
            val rows = tableObject.getJSONArray(table)
            totalRows += rows.length()
            require(totalRows <= MAX_BACKUP_ROWS) { "Backup contains too many rows to restore safely." }

            // name -> declared type, required, has SQL default. Older backup schemas may
            // legitimately omit columns added later, but only when SQLite can fill them safely.
            val schema = linkedMapOf<String, Triple<String, Boolean, Boolean>>()
            db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                val typeIndex = cursor.getColumnIndexOrThrow("type")
                val notNullIndex = cursor.getColumnIndexOrThrow("notnull")
                val pkIndex = cursor.getColumnIndexOrThrow("pk")
                val defaultIndex = cursor.getColumnIndexOrThrow("dflt_value")
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex)
                    val type = cursor.getString(typeIndex).uppercase()
                    val required = cursor.getInt(notNullIndex) == 1 || cursor.getInt(pkIndex) > 0
                    val hasDefault = !cursor.isNull(defaultIndex)
                    schema[name] = Triple(type, required, hasDefault)
                }
            }
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: error("Invalid row in backup table: $table")
                val keys = row.keys().asSequence().toSet()
                require(keys.all { it in schema }) { "Backup contains unknown columns in table: $table" }
                // Every exported row includes every column, including nullable columns. If a
                // field is absent, treating it as SQL NULL/default would silently discard data.
                // Require an exact column set so truncated or hand-edited rows fail preflight.
                if (backupSchemaVersion >= SCHEMA_VERSION) {
                    require(keys == schema.keys) {
                        "Backup row in $table is missing one or more database columns. The existing database has not been changed."
                    }
                } else {
                    schema.forEach { (column, definition) ->
                        if (column !in keys) require(!definition.second || definition.third) {
                            "Legacy backup omits required field $table.$column, which has no safe database default. The existing database has not been changed."
                        }
                    }
                }
                schema.forEach { (column, definition) ->
                    val (type, required, _) = definition
                    if (column in keys && required) require(row.has(column) && !row.isNull(column)) {
                        "Backup row in $table is missing required field: $column"
                    }
                    if (column in keys && row.has(column) && !row.isNull(column)) {
                        val value = row.get(column)
                        when {
                            type.contains("INT") -> require(value is Number || value is Boolean) {
                                "Invalid numeric field $table.$column in backup."
                            }
                            type.contains("REAL") || type.contains("FLOA") || type.contains("DOUB") ->
                                require(value is Number && value.toDouble().isFinite()) {
                                    "Invalid numeric field $table.$column in backup."
                                }
                            type.contains("BLOB") -> require(
                                value is JSONObject && value.has(BLOB_MARKER) &&
                                    runCatching { android.util.Base64.decode(value.getString(BLOB_MARKER), android.util.Base64.DEFAULT) }.isSuccess
                            ) { "Invalid binary field $table.$column in backup." }
                            else -> require(value is String || value is Number || value is Boolean) {
                                "Invalid field $table.$column in backup."
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Upgrade known pre-v10 backup payloads in memory after checksum verification.
     * Missing columns are left out so SQLite can apply declared defaults during insert.
     * No learner, assessment, mark, or audit row is discarded by this compatibility pass.
     */
    private fun normalizeLegacyBackupTables(tableObject: JSONObject, schemaVersion: Int) {
        // Only synthesize tables that did not exist in the declared source schema.
        // Filling a missing table in a current-schema backup would hide an incomplete or
        // tampered payload and could silently restore an empty teacher/admission roster.
        if (schemaVersion < 7) {
            setOf("teacher", "teacher_school_membership", "teaching_assignment")
                .filterNot(tableObject::has)
                .forEach { tableObject.put(it, JSONArray()) }
        }
        if (schemaVersion < 9 && !tableObject.has("school_admission_number")) {
            tableObject.put("school_admission_number", JSONArray())
        }

        if (schemaVersion < 7 && !tableObject.has("school")) {
            tableObject.put("school", JSONArray())
        }
        if (schemaVersion < 6 && !tableObject.has("learner_status_history")) {
            tableObject.put("learner_status_history", JSONArray())
        }
        if (schemaVersion < 10 && !tableObject.has("school_learner_status")) {
            tableObject.put("school_learner_status", JSONArray())
        }

        val schools = tableObject.optJSONArray("school") ?: JSONArray().also { tableObject.put("school", it) }
        val enrollments = tableObject.optJSONArray("enrollment") ?: JSONArray()
        val history = tableObject.optJSONArray("learner_status_history") ?: JSONArray()
        val learners = tableObject.optJSONArray("learner") ?: JSONArray()
        val learnerIdsWithEnrollment = mutableSetOf<String>()
        var requiresLegacySchool = false

        if (schemaVersion < 7) {
            for (i in 0 until enrollments.length()) {
                val row = enrollments.optJSONObject(i) ?: continue
                if (!row.has("school_id") || row.isNull("school_id") || row.optString("school_id").isBlank()) {
                    row.put("school_id", "legacy-school")
                }
            }
            // Pre-multischool backups need a stable school even when the exported roster is empty.
            requiresLegacySchool = true
        }
        for (i in 0 until enrollments.length()) {
            enrollments.optJSONObject(i)?.optString("learner_id")?.takeIf { it.isNotBlank() }?.let(learnerIdsWithEnrollment::add)
        }
        if (learners.length() > 0 && (0 until learners.length()).any { i ->
                learners.optJSONObject(i)?.optString("id")?.let { it !in learnerIdsWithEnrollment } == true
            }) {
            requiresLegacySchool = true
        }
        if (schemaVersion < 10) {
            for (i in 0 until history.length()) {
                history.optJSONObject(i)?.let { row ->
                    if (!row.has("school_id") || row.isNull("school_id") || row.optString("school_id").isBlank()) {
                        row.put("school_id", "legacy-school")
                        requiresLegacySchool = true
                    }
                }
            }
        }

        val legacySchoolExists = (0 until schools.length()).any { i ->
            schools.optJSONObject(i)?.optString("id") == "legacy-school"
        }
        if (requiresLegacySchool && !legacySchoolExists) {
            val now = System.currentTimeMillis()
            schools.put(JSONObject()
                .put("id", "legacy-school")
                .put("name", "")
                .put("motto", "")
                .put("created_at", now)
                .put("updated_at", now))
        }
    }

    private fun backfillSchoolStatusesAfterRestore(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL("""INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
            SELECT e.school_id, l.id, l.status, ?, ? FROM learner l JOIN enrollment e ON e.learner_id = l.id
            GROUP BY e.school_id, l.id""".trimIndent(), arrayOf(now, now))
        db.execSQL("""INSERT OR IGNORE INTO school_learner_status(school_id, learner_id, status, created_at, updated_at)
            SELECT 'legacy-school', l.id, l.status, ?, ? FROM learner l
            WHERE NOT EXISTS (SELECT 1 FROM enrollment e WHERE e.learner_id = l.id)""".trimIndent(), arrayOf(now, now))
    }

    private fun migrateLegacyAdmissionsInRestoredBackup(db: SQLiteDatabase) {
        val legacyRows = mutableListOf<Pair<String, String>>()
        db.rawQuery("SELECT id, admission_number FROM learner WHERE admission_number IS NOT NULL AND TRIM(admission_number) <> ''", null).use { c ->
            while (c.moveToNext()) legacyRows += c.getString(0) to c.getString(1)
        }
        legacyRows.forEach { (learnerId, raw) ->
            val admission = raw.trim().replace(Regex("\\s+"), " ")
            val normalized = admission.uppercase(java.util.Locale.ROOT)
            val schoolIds = mutableListOf<String>()
            db.rawQuery("SELECT DISTINCT school_id FROM enrollment WHERE learner_id = ?", arrayOf(learnerId)).use { c ->
                while (c.moveToNext()) schoolIds += c.getString(0)
            }
            if (schoolIds.isEmpty()) schoolIds += "legacy-school"
            schoolIds.forEach { schoolId ->
                db.execSQL(
                    "INSERT OR IGNORE INTO school_admission_number (id, school_id, learner_id, admission_number, normalized_admission_number, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(java.util.UUID.randomUUID().toString(), schoolId, learnerId, admission, normalized, System.currentTimeMillis(), System.currentTimeMillis())
                )
            }
        }
        db.execSQL("UPDATE learner SET admission_number = NULL WHERE admission_number IS NOT NULL")
    }

    private fun restoreTable(db: SQLiteDatabase, table: String, rows: JSONArray) {
        val schemaCursor = db.query(table, null, "1=0", null, null, null, null)
        val columns = schemaCursor.columnNames.toSet()
        schemaCursor.close()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            val values = android.content.ContentValues()
            val keys = row.keys()
            while (keys.hasNext()) {
                val column = keys.next()
                require(column in columns) { "Unknown column $table.$column in backup." }
                if (row.isNull(column)) values.putNull(column)
                else when (val value = row.get(column)) {
                    is Number -> values.put(column, value.toString())
                    is Boolean -> values.put(column, if (value) 1 else 0)
                    is JSONObject -> {
                        require(value.has(BLOB_MARKER)) { "Invalid binary field $table.$column in backup." }
                        values.put(column, android.util.Base64.decode(value.getString(BLOB_MARKER), android.util.Base64.DEFAULT))
                    }
                    else -> values.put(column, value.toString())
                }
            }
            db.insertOrThrow(table, null, values)
        }
    }
}
