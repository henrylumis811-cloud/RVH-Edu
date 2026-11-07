package com.henrylumis.rvhgrader.core.database

import android.database.sqlite.SQLiteDatabase
import com.henrylumis.rvhgrader.core.assessment.AssessmentPolicySnapshot
import com.henrylumis.rvhgrader.core.model.AssessmentStatus

/** Read-only integrity audit for the durable academic database. */
data class AcademicIntegrityReport(
    val healthy: Boolean,
    val problems: List<String>,
    val rowCounts: Map<String, Int>
)

object AcademicDatabaseIntegrity {
    fun audit(db: SQLiteDatabase): AcademicIntegrityReport {
        val problems = mutableListOf<String>()
        val counts = linkedMapOf<String, Int>()
        listOf("school", "learner", "school_admission_number", "enrollment", "assessment", "mark", "legacy_identity_review", "learner_status_history", "school_learner_status").forEach { table ->
            counts[table] = scalarInt(db, "SELECT COUNT(*) FROM $table")
        }

        val fk = db.rawQuery("PRAGMA foreign_key_check", null).use { it.count }
        if (fk != 0) problems += "$fk foreign-key violation(s)"

        val invalidMarks = scalarInt(db, "SELECT COUNT(*) FROM mark WHERE mark < 0 OR max_mark <= 0 OR mark > max_mark")
        if (invalidMarks != 0) problems += "$invalidMarks invalid mark(s)"

        val invalidSchoolAdmissions = scalarInt(db, """
            SELECT COUNT(*) FROM school_admission_number san
            LEFT JOIN school s ON s.id = san.school_id
            LEFT JOIN learner l ON l.id = san.learner_id
            WHERE s.id IS NULL OR l.id IS NULL OR TRIM(san.admission_number) = '' OR
                  san.normalized_admission_number <> UPPER(TRIM(san.admission_number))
        """.trimIndent())
        if (invalidSchoolAdmissions != 0) problems += "$invalidSchoolAdmissions invalid school-local admission assignment(s)"

        val invalidSchoolStatuses = scalarInt(db, """
            SELECT COUNT(*) FROM school_learner_status sls
            LEFT JOIN school s ON s.id = sls.school_id
            LEFT JOIN learner l ON l.id = sls.learner_id
            WHERE s.id IS NULL OR l.id IS NULL OR sls.status NOT IN ('ACTIVE','ARCHIVED')
        """.trimIndent())
        if (invalidSchoolStatuses != 0) problems += "$invalidSchoolStatuses invalid school-local learner status record(s)"

        // Every enrollment must have a lifecycle status for its exact school/learner pair.
        // A learner enrolled in two schools therefore needs two independent status rows.
        // Without this invariant, a roster could fall back to the legacy global learner.status
        // and make one school's archive decision appear to apply to another school.
        val enrollmentsMissingSchoolStatus = scalarInt(db, """
            SELECT COUNT(*) FROM enrollment e
            LEFT JOIN school_learner_status sls
              ON sls.school_id = e.school_id AND sls.learner_id = e.learner_id
            WHERE sls.school_id IS NULL
        """.trimIndent())
        if (enrollmentsMissingSchoolStatus != 0) {
            problems += "$enrollmentsMissingSchoolStatus enrollment(s) missing school-local learner status"
        }

        // Learners can be entered before their first enrollment. Their status may be
        // scoped to the currently selected school, so require at least one status row per
        // learner without forcing an artificial legacy-school attribution.
        val learnersMissingAnyStatus = scalarInt(db, """
            SELECT COUNT(*) FROM learner l
            WHERE NOT EXISTS (
                SELECT 1 FROM school_learner_status sls WHERE sls.learner_id = l.id
            )
        """.trimIndent())
        if (learnersMissingAnyStatus != 0) {
            problems += "$learnersMissingAnyStatus learner(s) missing every school-local lifecycle status"
        }

        val invalidSchoolStatusTimestamps = scalarInt(db, """
            SELECT COUNT(*) FROM school_learner_status
            WHERE created_at <= 0 OR updated_at <= 0 OR updated_at < created_at
        """.trimIndent())
        if (invalidSchoolStatusTimestamps != 0) {
            problems += "$invalidSchoolStatusTimestamps invalid school-local learner-status timestamp(s)"
        }

        val orphanLearnerEnrollments = scalarInt(db, """
            SELECT COUNT(*) FROM enrollment e
            LEFT JOIN learner l ON l.id = e.learner_id
            WHERE l.id IS NULL
        """.trimIndent())
        if (orphanLearnerEnrollments != 0) problems += "$orphanLearnerEnrollments orphan enrollment(s)"

        val orphanAssessments = scalarInt(db, """
            SELECT COUNT(*) FROM assessment a
            LEFT JOIN enrollment e ON e.id = a.enrollment_id
            WHERE e.id IS NULL
        """.trimIndent())
        if (orphanAssessments != 0) problems += "$orphanAssessments orphan assessment(s)"

        val orphanMarks = scalarInt(db, """
            SELECT COUNT(*) FROM mark m
            LEFT JOIN assessment a ON a.id = m.assessment_id
            WHERE a.id IS NULL
        """.trimIndent())
        if (orphanMarks != 0) problems += "$orphanMarks orphan mark(s)"

        // Completeness must be evaluated against the rules saved with the assessment, not
        // today's mutable subject catalog. Legacy assessments without a snapshot cannot be
        // judged reliably against current settings, so leave them untouched rather than
        // rejecting a valid historical backup after the school's subject list changes.
        var finalIncomplete = 0
        var invalidFinalPolicies = 0
        db.query(
            "assessment a JOIN enrollment e ON e.id = a.enrollment_id",
            arrayOf("a.id", "e.school_class", "a.policy_snapshot_json"),
            "a.status = ?", arrayOf(AssessmentStatus.FINAL.name), null, null, null
        ).use { c ->
            if (c.moveToFirst()) do {
                val assessmentId = c.getString(0)
                val enrollmentClass = c.getString(1)
                val snapshotJson = c.getString(2)
                if (!snapshotJson.isNullOrBlank()) {
                    val policy = runCatching { AssessmentPolicySnapshot.parse(snapshotJson) }.getOrNull()
                    if (policy == null || policy.schoolClass != enrollmentClass) {
                        invalidFinalPolicies++
                    } else {
                        val expectedCodes = policy.subjects.map { it.code.trim().uppercase() }.toSet()
                        val actualCodes = db.rawQuery(
                            "SELECT subject_code FROM mark WHERE assessment_id = ?", arrayOf(assessmentId)
                        ).use { marks ->
                            buildSet { while (marks.moveToNext()) add(marks.getString(0).trim().uppercase()) }
                        }
                        if (actualCodes != expectedCodes) finalIncomplete++
                    }
                }
            } while (c.moveToNext())
        }
        if (finalIncomplete != 0) problems += "$finalIncomplete final assessment(s) do not match their saved subject policy"
        if (invalidFinalPolicies != 0) problems += "$invalidFinalPolicies final assessment(s) have an invalid or class-mismatched saved policy"

        return AcademicIntegrityReport(problems.isEmpty(), problems, counts)
    }

    fun requireHealthy(db: SQLiteDatabase) {
        val report = audit(db)
        require(report.healthy) { "Academic database integrity check failed: ${report.problems.joinToString("; ")}" }
    }

    private fun scalarInt(db: SQLiteDatabase, sql: String, args: Array<String> = emptyArray()): Int =
        db.rawQuery(sql, args).use { if (it.moveToFirst()) it.getInt(0) else 0 }
}
