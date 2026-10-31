package com.henrylumis.rvhgrader.core.repository

import android.content.ContentValues
import android.database.Cursor
import android.content.Context
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.Enrollment
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

/** Persistence boundary for enrollments, assessments and marks. */
class AcademicRepository(private val db: RvhEduDatabase) {

    /**
     * Idempotently create an enrollment. Repeating an enrollment request must not reset its
     * original enrollment date or silently move a learner between streams.
     */
    fun upsertEnrollment(enrollment: Enrollment, schoolId: String): Enrollment {
        require(schoolId.isNotBlank()) { "A school must be selected before enrolling a learner" }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            data class Existing(val id: String, val stream: String?, val enrolledAt: Long)
            val existing = database.query(
                "enrollment", arrayOf("id", "stream", "enrolled_at"),
                "school_id = ? AND learner_id = ? AND academic_year = ? AND school_class = ?",
                arrayOf(schoolId, enrollment.learnerId, enrollment.academicYear.toString(), enrollment.schoolClass.name),
                null, null, null
            ).use { c ->
                if (!c.moveToFirst()) null else Existing(
                    c.getString(c.getColumnIndexOrThrow("id")),
                    c.getStringOrNull("stream"),
                    c.getLong(c.getColumnIndexOrThrow("enrolled_at"))
                )
            }

            if (existing != null) {
                val requestedStream = enrollment.stream?.trim()?.takeIf { it.isNotEmpty() }
                val storedStream = existing.stream?.trim()?.takeIf { it.isNotEmpty() }
                require(requestedStream == null || storedStream == null || requestedStream.equals(storedStream, ignoreCase = true)) {
                    "Learner is already enrolled in ${enrollment.schoolClass.displayName} for ${enrollment.academicYear} in stream $storedStream. Change the enrollment explicitly instead of overwriting it."
                }
                // A previously unspecified stream may be filled in, but an existing value and
                // the original enrollment timestamp are never overwritten by a repeated request.
                if (storedStream == null && requestedStream != null) {
                    database.update("enrollment", ContentValues().apply { put("stream", requestedStream) },
                        "id = ?", arrayOf(existing.id))
                }
                database.setTransactionSuccessful()
                return enrollment.copy(id = existing.id, stream = storedStream ?: requestedStream,
                    enrolledAtMillis = existing.enrolledAt)
            }

            val values = ContentValues().apply {
                put("id", enrollment.id)
                put("school_id", schoolId)
                put("learner_id", enrollment.learnerId)
                put("academic_year", enrollment.academicYear)
                put("school_class", enrollment.schoolClass.name)
                enrollment.stream?.trim()?.takeIf { it.isNotEmpty() }?.let { put("stream", it) } ?: putNull("stream")
                put("enrolled_at", enrollment.enrolledAtMillis)
            }
            database.insertOrThrow("enrollment", null, values)
            database.setTransactionSuccessful()
            return enrollment.copy(stream = enrollment.stream?.trim()?.takeIf { it.isNotEmpty() })
        } finally {
            database.endTransaction()
        }
    }


    /**
     * Insert a class-progression batch atomically. A learner may have only one enrollment in a
     * target academic year through this workflow; if any row conflicts, no row is committed.
     */
    fun insertEnrollmentsAtomically(
        enrollments: List<Enrollment>,
        expectedSourceEnrollmentIds: Map<String, String>? = null,
        sourceYear: Int? = null,
        sourceClass: SchoolClass? = null,
        schoolId: String
    ): List<Enrollment> {
        require(schoolId.isNotBlank()) { "A school must be selected before progressing learners" }
        require(enrollments.isNotEmpty()) { "Select at least one learner to progress" }
        require((expectedSourceEnrollmentIds == null && sourceYear == null && sourceClass == null) ||
            (expectedSourceEnrollmentIds != null && sourceYear != null && sourceClass != null)) {
            "Source enrollment validation requires the source year, class, and enrollment identities together"
        }
        if (expectedSourceEnrollmentIds != null) {
            require(expectedSourceEnrollmentIds.keys == enrollments.map { it.learnerId }.toSet()) {
                "Every selected learner must have a source enrollment identity"
            }
            require(sourceYear != null && sourceYear in 2000..2100) { "Invalid source academic year" }
        }
        require(enrollments.map { it.learnerId }.distinct().size == enrollments.size) {
            "The progression batch contains the same learner more than once"
        }
        require(enrollments.map { it.academicYear }.distinct().size == 1) {
            "A progression batch must use one target academic year"
        }
        require(enrollments.all { it.learnerId.isNotBlank() }) {
            "Every learner must have a valid identity before progression"
        }
        require(enrollments.all { it.academicYear in 2000..2100 }) {
            "Academic years must be between 2000 and 2100"
        }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            enrollments.forEach { enrollment ->
                // Recheck identity and active status inside the same transaction that inserts
                // the target enrollment. The roster may have changed after the UI preflight.
                val learnerStatus = database.query(
                    "learner", arrayOf("status"), "id = ?", arrayOf(enrollment.learnerId),
                    null, null, null
                ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
                require(learnerStatus == "ACTIVE") {
                    when (learnerStatus) {
                        null -> "Learner ${enrollment.learnerId} no longer exists. Reload the class before progressing learners."
                        else -> "Learner ${enrollment.learnerId} is archived or has an unsupported status. Restore and review the learner before progression."
                    }
                }
                // The selected roster may be stale. Verify the exact source enrollment inside
                // the same transaction as target insertion, not merely that the learner exists.
                if (expectedSourceEnrollmentIds != null) {
                    val expectedSourceId = expectedSourceEnrollmentIds.getValue(enrollment.learnerId)
                    val sourceStillMatches = database.query(
                        "enrollment", arrayOf("id"),
                        "id = ? AND school_id = ? AND learner_id = ? AND academic_year = ? AND school_class = ?",
                        arrayOf(expectedSourceId, schoolId, enrollment.learnerId, sourceYear.toString(), sourceClass!!.name),
                        null, null, null
                    ).use { c -> c.moveToFirst() }
                    require(sourceStillMatches) {
                        "The source enrollment for learner ${enrollment.learnerId} changed or no longer exists. Reload the class and review the selection."
                    }
                }
                val existing = database.query(
                    "enrollment", arrayOf("school_class"),
                    "school_id = ? AND learner_id = ? AND academic_year = ?",
                    arrayOf(schoolId, enrollment.learnerId, enrollment.academicYear.toString()),
                    null, null, null
                ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
                require(existing == null) {
                    "Learner ${enrollment.learnerId} already has an enrollment for ${enrollment.academicYear}; review the target year before retrying."
                }
                val values = ContentValues().apply {
                    put("id", enrollment.id)
                    put("school_id", schoolId)
                    put("learner_id", enrollment.learnerId)
                    put("academic_year", enrollment.academicYear)
                    put("school_class", enrollment.schoolClass.name)
                    enrollment.stream?.trim()?.takeIf { it.isNotEmpty() }?.let { put("stream", it) } ?: putNull("stream")
                    put("enrolled_at", enrollment.enrolledAtMillis)
                }
                database.insertOrThrow("enrollment", null, values)
            }
            database.setTransactionSuccessful()
            return enrollments
        } finally {
            database.endTransaction()
        }
    }

    fun getEnrollment(id: String): Enrollment? = db.readableDatabase.query("enrollment", null, "id = ?", arrayOf(id), null, null, null)
        .use { c -> if (!c.moveToFirst()) null else readEnrollment(c) }

    fun getEnrollmentsForLearner(learnerId: String, schoolId: String): List<Enrollment> =
        db.readableDatabase.query("enrollment", null, "learner_id = ? AND school_id = ?", arrayOf(learnerId, schoolId), null, null, "academic_year ASC, school_class ASC")
            .use { c -> buildList { if (c.moveToFirst()) do { add(readEnrollment(c)) } while (c.moveToNext()) } }

    fun getEnrollments(academicYear: Int, schoolClass: SchoolClass, schoolId: String): List<Enrollment> =
        db.readableDatabase.query("enrollment", null, "school_id = ? AND academic_year = ? AND school_class = ?",
            arrayOf(schoolId, academicYear.toString(), schoolClass.name), null, null, "enrolled_at ASC")
            .use { c -> buildList { if (c.moveToFirst()) do { add(readEnrollment(c)) } while (c.moveToNext()) } }

    fun upsertAssessment(assessment: AcademicAssessment): AcademicAssessment {
        val database = db.writableDatabase
        data class ExistingAssessment(val id: String, val policySnapshotJson: String?)
        val existing = database.query("assessment", arrayOf("id", "policy_snapshot_json"),
            "enrollment_id = ? AND term = ? AND section = ?",
            arrayOf(assessment.enrollmentId, assessment.term.name, assessment.section.name), null, null, null)
            .use { c -> if (c.moveToFirst()) ExistingAssessment(c.getString(0), c.getStringOrNull("policy_snapshot_json")) else null }
        val existingId = existing?.id
        val actualId = existingId ?: assessment.id
        // Once captured, a snapshot is immutable through ordinary upserts. Legacy null snapshots stay null unless explicitly created anew.
        val v = ContentValues().apply {
            put("id", actualId); put("enrollment_id", assessment.enrollmentId)
            put("term", assessment.term.name); put("section", assessment.section.name)
            put("recorded_at", assessment.recordedAtMillis)
            put("status", assessment.status.name)
            assessment.finalizedAtMillis?.let { put("finalized_at", it) } ?: putNull("finalized_at")
            (if (existing != null) existing.policySnapshotJson else assessment.policySnapshotJson)?.let { put("policy_snapshot_json", it) } ?: putNull("policy_snapshot_json")
        }
        if (existingId == null) database.insertOrThrow("assessment", null, v)
        else database.update("assessment", v, "id = ?", arrayOf(existingId))
        return assessment.copy(id = actualId, policySnapshotJson = if (existing != null) existing.policySnapshotJson else assessment.policySnapshotJson)
    }

    fun setAssessmentStatus(assessmentId: String, status: com.henrylumis.rvhgrader.core.model.AssessmentStatus, finalizedAtMillis: Long? = null) {
        val values = ContentValues().apply {
            put("status", status.name)
            if (status == com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL) {
                put("finalized_at", finalizedAtMillis ?: System.currentTimeMillis())
            } else putNull("finalized_at")
        }
        val changed = db.writableDatabase.update("assessment", values, "id = ?", arrayOf(assessmentId))
        check(changed == 1) { "Assessment not found: $assessmentId" }
    }

    fun getAssessment(enrollmentId: String, term: Term, section: TermSection): AcademicAssessment? =
        db.readableDatabase.query("assessment", null, "enrollment_id = ? AND term = ? AND section = ?",
            arrayOf(enrollmentId, term.name, section.name), null, null, null)
            .use { c -> if (!c.moveToFirst()) null else readAssessment(c) }

    /** Replace marks only while the persisted assessment is still a draft. */
    fun replaceDraftMarks(assessmentId: String, marks: List<Mark>) {
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requirePersistedDraft(database, assessmentId)
            replaceMarksInsideTransaction(database, assessmentId, marks)
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
    }

    /** Save all marks and finalize in one transaction; no partial finalization is possible. */
    fun replaceMarksAndFinalize(assessmentId: String, marks: List<Mark>) {
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requirePersistedDraft(database, assessmentId)
            replaceMarksInsideTransaction(database, assessmentId, marks)
            val values = ContentValues().apply {
                put("status", com.henrylumis.rvhgrader.core.model.AssessmentStatus.FINAL.name)
                put("finalized_at", System.currentTimeMillis())
            }
            val changed = database.update("assessment", values, "id = ? AND status = ?",
                arrayOf(assessmentId, com.henrylumis.rvhgrader.core.model.AssessmentStatus.DRAFT.name))
            check(changed == 1) { "Assessment is no longer a draft; reload it before saving." }
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
    }

    private fun requirePersistedDraft(database: android.database.sqlite.SQLiteDatabase, assessmentId: String) {
        val status = database.query("assessment", arrayOf("status"), "id = ?", arrayOf(assessmentId), null, null, null).use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
        require(status == com.henrylumis.rvhgrader.core.model.AssessmentStatus.DRAFT.name) {
            if (status == null) "Assessment not found: $assessmentId" else "This assessment is final and locked. Reopen it before editing."
        }
    }

    private fun replaceMarksInsideTransaction(database: android.database.sqlite.SQLiteDatabase, assessmentId: String, marks: List<Mark>) {
        val seen = mutableSetOf<String>()
        marks.forEach { mark ->
            require(mark.assessmentId == assessmentId) { "Mark belongs to another assessment" }
            require(seen.add(mark.subjectCode)) { "Duplicate subject: ${mark.subjectCode}" }
            require(mark.maxMark > 0 && mark.mark in 0..mark.maxMark) { "Mark must be between 0 and max mark" }
        }
        database.delete("mark", "assessment_id = ?", arrayOf(assessmentId))
        marks.forEach { mark ->
            database.insertOrThrow("mark", null, ContentValues().apply {
                put("id", mark.id); put("assessment_id", mark.assessmentId); put("subject_code", mark.subjectCode)
                put("mark", mark.mark); put("max_mark", mark.maxMark)
                mark.aggregate?.let { put("aggregate", it) } ?: putNull("aggregate")
                put("source", mark.source); put("updated_at", mark.updatedAtMillis)
            })
        }
    }

    fun getAssessmentsForEnrollment(enrollmentId: String): List<AcademicAssessment> =
        db.readableDatabase.query("assessment", null, "enrollment_id = ?", arrayOf(enrollmentId), null, null, "term ASC, section ASC")
            .use { c -> buildList { if (c.moveToFirst()) do { add(readAssessment(c)) } while (c.moveToNext()) } }

    fun getMarks(assessmentId: String): List<Mark> = db.readableDatabase.query("mark", null,
        "assessment_id = ?", arrayOf(assessmentId), null, null, "subject_code ASC")
        .use { c -> buildList { if (c.moveToFirst()) do { add(readMark(c)) } while (c.moveToNext()) } }

    /** School-verified assessment lookup. The enrollment is checked before assessment data is exposed. */
    fun getAssessmentForSchool(enrollmentId: String, term: Term, section: TermSection, schoolId: String): AcademicAssessment? {
        require(schoolId.isNotBlank()) { "A school must be selected before reading assessments" }
        requireEnrollmentBelongsToSchool(enrollmentId, schoolId)
        return getAssessment(enrollmentId, term, section)
    }

    /** School-verified assessment history lookup. */
    fun getAssessmentsForEnrollmentInSchool(enrollmentId: String, schoolId: String): List<AcademicAssessment> {
        require(schoolId.isNotBlank()) { "A school must be selected before reading assessment history" }
        requireEnrollmentBelongsToSchool(enrollmentId, schoolId)
        return getAssessmentsForEnrollment(enrollmentId)
    }

    /** School-verified mark read; prevents a foreign-school assessment ID from exposing marks. */
    fun getMarksForSchool(assessmentId: String, schoolId: String): List<Mark> {
        require(schoolId.isNotBlank()) { "A school must be selected before reading marks" }
        val belongs = db.readableDatabase.rawQuery(
            "SELECT 1 FROM assessment a JOIN enrollment e ON e.id = a.enrollment_id WHERE a.id = ? AND e.school_id = ? LIMIT 1",
            arrayOf(assessmentId, schoolId)
        ).use { it.moveToFirst() }
        require(belongs) { "Assessment does not belong to the selected school" }
        return getMarks(assessmentId)
    }

    /** Create/update an assessment only when its enrollment belongs to the selected school. */
    fun upsertAssessmentForSchool(assessment: AcademicAssessment, schoolId: String): AcademicAssessment {
        require(schoolId.isNotBlank()) { "A school must be selected before saving assessments" }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requireEnrollmentBelongsToSchool(assessment.enrollmentId, schoolId)
            val saved = upsertAssessment(assessment)
            database.setTransactionSuccessful()
            return saved
        } finally { database.endTransaction() }
    }

    /** Replace draft marks only after verifying the assessment-school relationship in the write transaction. */
    fun replaceDraftMarksForSchool(assessmentId: String, marks: List<Mark>, schoolId: String) {
        require(schoolId.isNotBlank()) { "A school must be selected before saving marks" }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requireAssessmentBelongsToSchool(database, assessmentId, schoolId)
            replaceDraftMarks(assessmentId, marks)
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
    }

    /** Replace marks and finalize only after verifying school ownership in the write transaction. */
    fun replaceMarksAndFinalizeForSchool(assessmentId: String, marks: List<Mark>, schoolId: String) {
        require(schoolId.isNotBlank()) { "A school must be selected before finalizing assessments" }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requireAssessmentBelongsToSchool(database, assessmentId, schoolId)
            replaceMarksAndFinalize(assessmentId, marks)
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
    }

    /** Change assessment status only for an assessment owned by the selected school. */
    fun setAssessmentStatusForSchool(assessmentId: String, status: com.henrylumis.rvhgrader.core.model.AssessmentStatus, schoolId: String, finalizedAtMillis: Long? = null) {
        require(schoolId.isNotBlank()) { "A school must be selected before changing assessment status" }
        val database = db.writableDatabase
        database.beginTransaction()
        try {
            requireAssessmentBelongsToSchool(database, assessmentId, schoolId)
            setAssessmentStatus(assessmentId, status, finalizedAtMillis)
            database.setTransactionSuccessful()
        } finally { database.endTransaction() }
    }

    private fun requireAssessmentBelongsToSchool(database: android.database.sqlite.SQLiteDatabase, assessmentId: String, schoolId: String) {
        val belongs = database.rawQuery(
            "SELECT 1 FROM assessment a JOIN enrollment e ON e.id = a.enrollment_id WHERE a.id = ? AND e.school_id = ? LIMIT 1",
            arrayOf(assessmentId, schoolId)
        ).use { it.moveToFirst() }
        require(belongs) { "Assessment does not belong to the selected school" }
    }

    private fun requireEnrollmentBelongsToSchool(enrollmentId: String, schoolId: String) {
        val belongs = db.readableDatabase.query(
            "enrollment", arrayOf("id"), "id = ? AND school_id = ?", arrayOf(enrollmentId, schoolId), null, null, null
        ).use { it.moveToFirst() }
        require(belongs) { "Enrollment does not belong to the selected school" }
    }

    fun close() = db.close()

    private fun readEnrollment(c: Cursor) = Enrollment(c.getString(c.getColumnIndexOrThrow("id")),
        c.getString(c.getColumnIndexOrThrow("learner_id")), c.getInt(c.getColumnIndexOrThrow("academic_year")),
        SchoolClass.valueOf(c.getString(c.getColumnIndexOrThrow("school_class"))), c.getStringOrNull("stream"),
        c.getLong(c.getColumnIndexOrThrow("enrolled_at")))

    private fun readAssessment(c: Cursor) = AcademicAssessment(c.getString(c.getColumnIndexOrThrow("id")),
        c.getString(c.getColumnIndexOrThrow("enrollment_id")), Term.valueOf(c.getString(c.getColumnIndexOrThrow("term"))),
        TermSection.valueOf(c.getString(c.getColumnIndexOrThrow("section"))),
        com.henrylumis.rvhgrader.core.model.AssessmentStatus.valueOf(c.getString(c.getColumnIndexOrThrow("status"))),
        c.getLong(c.getColumnIndexOrThrow("recorded_at")), c.getLongOrNull("finalized_at"),
        c.getStringOrNull("policy_snapshot_json"))

    private fun readMark(c: Cursor) = Mark(c.getString(c.getColumnIndexOrThrow("id")),
        c.getString(c.getColumnIndexOrThrow("assessment_id")), c.getString(c.getColumnIndexOrThrow("subject_code")),
        c.getInt(c.getColumnIndexOrThrow("mark")), c.getInt(c.getColumnIndexOrThrow("max_mark")),
        c.getIntOrNull("aggregate"), c.getString(c.getColumnIndexOrThrow("source")), c.getLong(c.getColumnIndexOrThrow("updated_at")))

    private fun Cursor.getIntOrNull(name: String): Int? { val i = getColumnIndexOrThrow(name); return if (isNull(i)) null else getInt(i) }
    private fun Cursor.getStringOrNull(name: String): String? { val i = getColumnIndexOrThrow(name); return if (isNull(i)) null else getString(i) }
    private fun Cursor.getLongOrNull(name: String): Long? { val i = getColumnIndexOrThrow(name); return if (isNull(i)) null else getLong(i) }
}
