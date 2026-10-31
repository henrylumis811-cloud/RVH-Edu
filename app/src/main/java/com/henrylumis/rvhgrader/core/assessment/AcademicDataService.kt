package com.henrylumis.rvhgrader.core.assessment

import android.content.Context
import android.content.ContentValues
import java.util.UUID
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.assessment.AssessmentPolicySnapshot
import com.henrylumis.rvhgrader.core.grading.RankingEngine
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.*
import com.henrylumis.rvhgrader.core.repository.AcademicRepository
import com.henrylumis.rvhgrader.core.repository.LearnerRepository
import com.henrylumis.rvhgrader.data.SchoolManagementRepository
import com.henrylumis.rvhgrader.model.*

/** Durable application service for the core learner -> assessment -> marks -> results pipeline. */
class AcademicDataService(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private fun activeSchoolId(): String = SchoolManagementRepository.activeId(appContext)
    private val database = RvhEduDatabase(context)
    private val learnerRepository = LearnerRepository(database)
    private val academicRepository = AcademicRepository(database)

    fun createLearner(name: String, ageYears: Int? = null, gender: String? = null, admissionNumber: String? = null): Learner {
        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        require(cleanName.isNotEmpty()) { "Learner name is required" }
        require(ageYears == null || ageYears in 2..30) { "Learner age must be between 2 and 30" }
        val cleanAdmissionNumber = admissionNumber?.trim()?.replace(Regex("\\s+"), " ")
            ?.takeIf { it.isNotEmpty() }?.uppercase()
        if (cleanAdmissionNumber != null && learnerRepository.findByAdmissionNumberInSchool(cleanAdmissionNumber, activeSchoolId()) != null) {
            throw IllegalArgumentException("Admission number $cleanAdmissionNumber is already assigned to another learner in this school")
        }
        val learner = Learner(fullName = cleanName, ageYears = ageYears,
            gender = gender?.trim()?.takeIf { it.isNotEmpty() },
            admissionNumber = cleanAdmissionNumber)
        learnerRepository.upsert(learner, activeSchoolId())
        return learner
    }

    /** Create a learner and their first class enrolment atomically; failed enrolment leaves no orphan learner. */
    fun createAndEnrollLearner(
        name: String, academicYear: Int, schoolClass: SchoolClass, ageYears: Int? = null,
        admissionNumber: String? = null, stream: String? = null
    ): Learner {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val learner = createLearner(name, ageYears = ageYears, admissionNumber = admissionNumber)
            enroll(learner.id, academicYear, schoolClass, stream)
            db.setTransactionSuccessful()
            return learner
        } finally {
            db.endTransaction()
        }
    }

    /** Returns review-only possible duplicate matches; this never modifies learner records. */
    fun findPossibleLearnerDuplicates(
        name: String,
        ageYears: Int? = null,
        admissionNumber: String? = null,
        excludingLearnerId: String? = null
    ): List<com.henrylumis.rvhgrader.core.repository.PossibleLearnerDuplicate> {
        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        if (cleanName.isBlank()) return emptyList()
        val proposed = Learner(
            id = excludingLearnerId?.trim()?.takeIf { it.isNotEmpty() } ?: "__new_learner_review__",
            fullName = cleanName,
            ageYears = ageYears,
            admissionNumber = admissionNumber?.trim()?.takeIf { it.isNotEmpty() }
        )
        return learnerRepository.findPossibleDuplicates(proposed, activeSchoolId())
    }

    fun enroll(learnerId: String, academicYear: Int, schoolClass: SchoolClass, stream: String? = null): Enrollment {
        val learner = learnerRepository.getById(learnerId) ?: error("Learner does not exist")
        require(learner.status == "ACTIVE") { "Archived learners cannot be enrolled. Restore the learner first." }
        require(academicYear in 2000..2100) { "Invalid academic year" }
        val cleanStream = stream?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotEmpty() }
        return academicRepository.upsertEnrollment(Enrollment(learnerId = learnerId, academicYear = academicYear,
            schoolClass = schoolClass, stream = cleanStream), schoolId = activeSchoolId())
    }

    /** Progress selected active learners into a new academic year without touching past records. */
    fun progressClass(
        sourceYear: Int,
        sourceClass: SchoolClass,
        targetYear: Int,
        targetClass: SchoolClass,
        learnerIds: Set<String>
    ): Int {
        require(sourceYear in 2000..2100 && targetYear in 2000..2100) { "Academic years must be between 2000 and 2100" }
        require(targetYear > sourceYear) { "The target academic year must be later than the source year" }
        require(learnerIds.isNotEmpty()) { "Select at least one learner to progress" }
        val sourceRoster = roster(sourceYear, sourceClass).associateBy { it.learner.id }
        require(learnerIds.all { it in sourceRoster }) { "The roster changed. Reload the class and review the selection." }
        val selected = learnerIds.map { id ->
            val learner = sourceRoster.getValue(id).learner
            require(learner.status == "ACTIVE") { "Archived learners must be restored before progression." }
            Enrollment(
                learnerId = learner.id,
                academicYear = targetYear,
                schoolClass = targetClass,
                stream = sourceRoster.getValue(id).enrollment.stream
            )
        }
        val sourceEnrollmentIds = learnerIds.associateWith { id -> sourceRoster.getValue(id).enrollment.id }
        academicRepository.insertEnrollmentsAtomically(
            enrollments = selected,
            expectedSourceEnrollmentIds = sourceEnrollmentIds,
            sourceYear = sourceYear,
            sourceClass = sourceClass,
            schoolId = activeSchoolId()
        )
        return selected.size
    }

    fun findAssessment(enrollment: Enrollment, term: Term, section: TermSection): AcademicAssessment? =
        academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())

    fun openAssessment(enrollment: Enrollment, term: Term, section: TermSection): AcademicAssessment =
        academicRepository.upsertAssessmentForSchool(academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())
            ?: AcademicAssessment(
                enrollmentId = enrollment.id,
                term = term,
                section = section,
                policySnapshotJson = AssessmentPolicySnapshot.capture(appContext, enrollment.schoolClass).toJson()
            ), activeSchoolId())

    fun saveMarks(assessment: AcademicAssessment, enrollment: Enrollment, inputs: List<AssessmentWorkflow.MarkInput>, scale: GradingScale = GradingScale.default()): AcademicResult {
        val persisted = academicRepository.getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())
            ?: error("Assessment no longer exists. Reload the assessment before saving.")
        require(persisted.id == assessment.id && assessment.enrollmentId == enrollment.id) {
            "Assessment or learner enrollment changed. Reload before saving."
        }
        require(persisted.status == AssessmentStatus.DRAFT && assessment.status == AssessmentStatus.DRAFT) {
            "This assessment is final and locked. Reopen it before editing."
        }
        // A non-null snapshot is an explicit historical claim. If it is malformed, fail closed
        // rather than silently scoring the assessment with today's active rules.
        val snapshot = persisted.policySnapshotJson?.let { json ->
            try { AssessmentPolicySnapshot.parse(json) }
            catch (e: Exception) {
                throw IllegalStateException("This assessment's saved academic policy is invalid. Marks were not saved; review the policy record before continuing.", e)
            }
        }
        val expectedSubjects = if (snapshot != null) snapshot.subjects.map { it.code } else AssessmentWorkflow.subjectsFor(enrollment.schoolClass)
        val suppliedCodes = inputs.map { it.subjectCode }
        require(suppliedCodes.distinct().size == suppliedCodes.size) { "Duplicate subjects are not allowed." }
        require(suppliedCodes.all { it in expectedSubjects }) { "Marks contain a subject not assigned to this assessment's saved subject list." }
        val marks = AssessmentWorkflow.toMarks(assessment.id, inputs)
        val effectiveScale = if (snapshot?.hasCompleteScoringPolicy == true) snapshot.gradingScale() else scale
        val result = com.henrylumis.rvhgrader.core.grading.AcademicEngine.calculate(
            learnerId = enrollment.learnerId,
            assessmentId = assessment.id,
            mode = enrollment.schoolClass.mode,
            subjectCodes = expectedSubjects,
            marks = marks,
            scale = effectiveScale
        )
        require(result.complete) { "Cannot finalize: missing ${result.missingSubjects.joinToString(", ")}" }
        academicRepository.replaceMarksAndFinalizeForSchool(assessment.id, marks, activeSchoolId())
        return result
    }

    fun loadMarks(assessment: AcademicAssessment): List<Mark> = academicRepository.getMarksForSchool(assessment.id, activeSchoolId())
    data class ImportResult(
        val importedRows: Int,
        val createdLearners: Int,
        val updatedLearners: Int,
        val ambiguousRows: Int,
        val skippedRows: Int
    )

    fun importReviewedSpreadsheetRows(
        year: Int,
        schoolClass: SchoolClass,
        term: Term,
        section: TermSection,
        rows: List<com.henrylumis.rvhgrader.core.importer.ImportReviewRow>
    ): ImportResult {
        var imported = 0
        var created = 0
        var updated = 0
        var ambiguous = 0
        var skipped = 0
        rows.filter { it.included }.forEach { row ->
            if (row.name.isBlank() || row.marks.values.none { it != null }) { skipped++; return@forEach }
            // Re-resolve the edited name against the current roster. The review UI may have
            // changed the name after the initial preview, so never trust stale candidates.
            val currentCandidates = roster(year, schoolClass).filter {
                normalizeName(it.learner.fullName) == normalizeName(row.name)
            }
            if (currentCandidates.size > 1) { ambiguous++; return@forEach }
            val learner = currentCandidates.singleOrNull()?.learner
                ?: createLearner(row.name).also { created++ }
            if (currentCandidates.size == 1) updated++
            val enrollment = currentCandidates.singleOrNull()?.enrollment
                ?: enroll(learner.id, year, schoolClass)
            val assessment = openAssessment(enrollment, term, section)
            row.marks.forEach { (code, value) ->
                if (value != null && code in AssessmentWorkflow.subjectsFor(schoolClass)) {
                    saveDraftMarks(assessment, enrollment, listOf(AssessmentWorkflow.MarkInput(code, value, source = "SPREADSHEET_REVIEWED")))
                }
            }
            imported++
        }
        return ImportResult(imported, created, updated, ambiguous, skipped)
    }

    private fun normalizeName(value: String): String =
        value.trim().replace(Regex("\\s+"), " ").uppercase()


    fun saveDraftMarks(assessment: AcademicAssessment, enrollment: Enrollment, inputs: List<AssessmentWorkflow.MarkInput>): List<Mark> {
        val persisted = academicRepository.getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())
            ?: error("Assessment no longer exists. Reload the assessment before saving.")
        require(persisted.id == assessment.id && assessment.enrollmentId == enrollment.id) {
            "Assessment or learner enrollment changed. Reload before saving."
        }
        require(persisted.status == AssessmentStatus.DRAFT && assessment.status == AssessmentStatus.DRAFT) {
            "This assessment is final and locked. Reopen it before editing."
        }
        val expectedCodes = savedSubjectCodes(persisted, enrollment.schoolClass).toSet()
        require(inputs.all { it.subjectCode in expectedCodes }) { "Marks contain a subject not assigned to this assessment's saved subject list." }
        require(inputs.size == 1) { "Draft save expects one subject at a time" }
        val input = inputs.single()
        val existing = academicRepository.getMarksForSchool(assessment.id, activeSchoolId()).toMutableList()
        existing.removeAll { it.subjectCode == input.subjectCode }
        val newMark = AssessmentWorkflow.toMarks(assessment.id, listOf(input)).singleOrNull()
        if (newMark != null) existing += newMark
        academicRepository.replaceDraftMarksForSchool(assessment.id, existing, activeSchoolId())
        return existing
    }

    /** Save the complete individual mark sheet as one atomic replacement. Blank subjects are omitted. */
    fun saveDraftMarkSet(
        assessment: AcademicAssessment,
        enrollment: Enrollment,
        inputs: List<AssessmentWorkflow.MarkInput>
    ): List<Mark> {
        val persisted = academicRepository.getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())
            ?: error("Assessment no longer exists. Reload the assessment before saving.")
        require(persisted.id == assessment.id && assessment.enrollmentId == enrollment.id) {
            "Assessment or learner enrollment changed. Reload before saving."
        }
        require(persisted.status == AssessmentStatus.DRAFT && assessment.status == AssessmentStatus.DRAFT) {
            "This assessment is final and locked. Reopen it before editing."
        }
        val expected = savedSubjectCodes(persisted, enrollment.schoolClass).toSet()
        require(inputs.all { it.subjectCode in expected }) { "Marks contain a subject not assigned to this assessment's saved subject list." }
        val supplied = inputs.map { it.subjectCode }
        require(supplied.size == expected.size && supplied.toSet() == expected) {
            "The mark sheet is incomplete. Reload it and try again."
        }
        val marks = AssessmentWorkflow.toMarks(assessment.id, inputs)
        // Repository wraps deletion and insertion in a single SQLite transaction.
        academicRepository.replaceDraftMarksForSchool(assessment.id, marks, activeSchoolId())
        return marks
    }

    fun reopenAssessment(assessment: AcademicAssessment): AcademicAssessment {
        academicRepository.setAssessmentStatusForSchool(assessment.id, AssessmentStatus.DRAFT, activeSchoolId())
        return assessment.copy(status = AssessmentStatus.DRAFT, finalizedAtMillis = null)
    }

    fun assessmentHistory(enrollment: Enrollment, scale: GradingScale = GradingScale.default()): List<com.henrylumis.rvhgrader.core.analytics.AssessmentSnapshot> =
        academicRepository.getAssessmentsForEnrollmentInSchool(enrollment.id, activeSchoolId()).map { assessment ->
            val marks = academicRepository.getMarksForSchool(assessment.id, activeSchoolId())
            val result = calculate(enrollment, assessment, scale)
            com.henrylumis.rvhgrader.core.analytics.AssessmentSnapshot(
                learnerId = enrollment.learnerId, academicYear = enrollment.academicYear,
                termOrder = assessment.term.ordinal, sectionOrder = assessment.section.ordinal,
                assessmentLabel = "${assessment.term.displayName} • ${assessment.section.displayName}",
                result = result, marks = marks
            )
        }


    /** Calculate from the assessment's immutable policy snapshot whenever one is available. */
    fun calculate(enrollment: Enrollment, assessment: AcademicAssessment, scale: GradingScale = GradingScale.default()): AcademicResult {
        val saved = academicRepository.getAssessmentForSchool(enrollment.id, assessment.term, assessment.section, activeSchoolId())
        val canonical = saved?.takeIf { it.id == assessment.id } ?: assessment
        val policy = parsePolicy(canonical)
        val subjects = policy?.subjects?.map { it.code } ?: AssessmentWorkflow.subjectsFor(enrollment.schoolClass)
        val effectiveScale = policy?.takeIf { it.hasCompleteScoringPolicy }?.gradingScale() ?: scale
        return com.henrylumis.rvhgrader.core.grading.AcademicEngine.calculate(
            learnerId = enrollment.learnerId,
            assessmentId = canonical.id,
            mode = enrollment.schoolClass.mode,
            subjectCodes = subjects,
            marks = academicRepository.getMarksForSchool(canonical.id, activeSchoolId()),
            scale = effectiveScale
        )
    }

    /** Null means a genuinely legacy/unpinned record; malformed non-null JSON must never masquerade as legacy. */
    private fun parsePolicy(assessment: AcademicAssessment): AssessmentPolicySnapshot? =
        assessment.policySnapshotJson?.let { json ->
            try { AssessmentPolicySnapshot.parse(json) }
            catch (e: Exception) {
                throw IllegalStateException("Assessment ${assessment.id} has an invalid saved academic policy. Historical results cannot be calculated safely.", e)
            }
        }

    private fun savedSubjectCodes(assessment: AcademicAssessment, schoolClass: SchoolClass): List<String> =
        parsePolicy(assessment)?.subjects?.map { it.code } ?: AssessmentWorkflow.subjectsFor(schoolClass)

    fun classAnalytics(
        year: Int,
        schoolClass: SchoolClass,
        term: Term,
        section: TermSection,
        scale: GradingScale = GradingScale.default(),
        passPercent: Double? = null
    ): com.henrylumis.rvhgrader.core.analytics.ClassAnalyticsSummary {
        val enrollments = academicRepository.getEnrollments(year, schoolClass, activeSchoolId())
        val assessments = enrollments.mapNotNull { academicRepository.getAssessmentForSchool(it.id, term, section, activeSchoolId()) }
        val policyError = AssessmentPolicySnapshot.classCompatibilityErrorForLearners(
            enrollments.mapNotNull { enrollment ->
                val assessment = academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())
                assessment?.let { (learnerRepository.getById(enrollment.learnerId)?.fullName ?: enrollment.learnerId) to it.policySnapshotJson }
            }
        )
        require(policyError == null) { policyError ?: "Assessment policies are incompatible." }
        val ranked = classResults(year, schoolClass, term, section, scale)
        val selectedPolicy = assessments.asSequence().mapNotNull { parsePolicy(it) }.firstOrNull()
        val effectivePassPercent = passPercent ?: selectedPolicy?.classPassPercent ?: 50.0
        val expectedCodes = selectedPolicy?.subjects?.map { it.code }
            ?: SubjectCatalog.forClass(schoolClass).map { it.code }
        val marksByLearner = enrollments.associate { enrollment ->
            val assessment = academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())
            enrollment.learnerId to (assessment?.let { academicRepository.getMarksForSchool(it.id, activeSchoolId()) } ?: emptyList())
        }
        val progress = academicRepository.getEnrollments(year, schoolClass, activeSchoolId()).mapNotNull { enrollment ->
            // Analytics for a selected assessment must be a historical snapshot. Later terms
            // must never influence the risk/trend shown for an earlier term.
            val cutoffYear = year
            val cutoffTerm = term.ordinal
            val cutoffSection = section.ordinal
            val snapshots = learnerHistory(enrollment.learnerId, scale)?.enrollments.orEmpty()
                .flatMap { it.assessments }
                .map { it.snapshot }
                .filter { snapshot ->
                    snapshot.academicYear < cutoffYear ||
                        (snapshot.academicYear == cutoffYear &&
                            (snapshot.termOrder < cutoffTerm ||
                                (snapshot.termOrder == cutoffTerm && snapshot.sectionOrder <= cutoffSection)))
                }
            com.henrylumis.rvhgrader.core.analytics.AcademicAnalytics.learnerProgress(snapshots)
                .firstOrNull { it.learnerId == enrollment.learnerId }
        }
        return com.henrylumis.rvhgrader.core.analytics.ClassAnalyticsCalculator.summarize(
            ranked,
            marksByLearner,
            progress,
            passPercent = effectivePassPercent,
            expectedSubjectCodes = expectedCodes,
            gradingScale = scale
        )
    }

    fun classResults(year: Int, schoolClass: SchoolClass, term: Term, section: TermSection, scale: GradingScale = GradingScale.default()): List<RankedResult> {
        val enrollments = academicRepository.getEnrollments(year, schoolClass, activeSchoolId())
        val assessments = enrollments.map { enrollment ->
            academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())
                ?: AcademicAssessment(enrollmentId = enrollment.id, term = term, section = section)
        }
        val policyError = AssessmentPolicySnapshot.classCompatibilityErrorForLearners(
            enrollments.mapNotNull { enrollment ->
                val assessment = academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId())
                assessment?.let { (learnerRepository.getById(enrollment.learnerId)?.fullName ?: enrollment.learnerId) to it.policySnapshotJson }
            }
        )
        require(policyError == null) { policyError ?: "Assessment policies are incompatible." }
        val results = enrollments.zip(assessments).map { (enrollment, assessment) ->
            calculate(enrollment, assessment, scale)
        }
        return RankingEngine.rankIncludingIncomplete(results)
    }

    /**
     * Search only the active school's roster. Learner identity is shared across schools, but
     * roster discovery must not leak names from unrelated schools into this school's screens.
     */
    fun findLearners(name: String, ageYears: Int? = null): List<Learner> {
        val normalizedName = name.trim().replace(Regex("\\s+"), " ")
        if (normalizedName.isBlank()) return emptyList()
        return learnerRepository.getEnrolledLearnersForSchool(activeSchoolId()).filter { learner ->
            learner.fullName.trim().replace(Regex("\\s+"), " ").equals(normalizedName, ignoreCase = true) &&
                (ageYears == null || learner.ageYears == ageYears)
        }
    }

    /** The learner directory is scoped to the selected school's enrollment history. */
    fun allLearners(): List<Learner> =
        learnerRepository.getEnrolledLearnersForSchool(activeSchoolId())

    /** Update editable learner details without replacing identity or academic history. */
    fun updateLearnerDetails(
        learnerId: String,
        name: String,
        ageYears: Int? = null,
        admissionNumber: String? = null
    ): Learner {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val existing = learnerRepository.getById(learnerId, activeSchoolId()) ?: error("Learner not found")
            val cleanName = name.trim().replace(Regex("\\s+"), " ")
            require(cleanName.isNotEmpty()) { "Learner name is required" }
            require(ageYears == null || ageYears in 2..30) { "Learner age must be between 2 and 30" }
            val cleanAdmissionNumber = admissionNumber?.trim()?.replace(Regex("\\s+"), " ")
                ?.takeIf { it.isNotEmpty() }?.uppercase()
            if (cleanAdmissionNumber != null) {
                val duplicate = learnerRepository.findByAdmissionNumberInSchool(cleanAdmissionNumber, activeSchoolId())
                require(duplicate == null || duplicate.id == learnerId) {
                    "Admission number $cleanAdmissionNumber is already assigned to another learner"
                }
            }
            val updated = existing.copy(
                fullName = cleanName,
                ageYears = ageYears,
                admissionNumber = cleanAdmissionNumber,
                updatedAtMillis = System.currentTimeMillis()
            )
            learnerRepository.upsert(updated, activeSchoolId())
            db.setTransactionSuccessful()
            return updated
        } finally {
            db.endTransaction()
        }
    }

    /** Archive a learner with a required reason and durable audit event; academic history is preserved. */
    fun archiveLearner(learnerId: String, reason: String = "Removed from active roster", note: String? = null) {
        val cleanReason = reason.trim().replace(Regex("\\s+"), " ")
        require(cleanReason.isNotEmpty()) { "Choose or enter a reason before archiving this learner." }
        require(cleanReason.length <= 120) { "Archive reason must be 120 characters or fewer." }
        val cleanNote = note?.trim()?.takeIf { it.isNotEmpty() }
        require(cleanNote == null || cleanNote.length <= 500) { "Archive note must be 500 characters or fewer." }
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val learner = learnerRepository.getById(learnerId, activeSchoolId()) ?: error("Learner not found")
            require(learner.status == "ACTIVE" || learner.status == "ARCHIVED") {
                "Learner has an unsupported status; review the record before archiving."
            }
            if (learner.status == "ACTIVE") {
                val now = System.currentTimeMillis()
                learnerRepository.setStatusInSchool(learnerId, activeSchoolId(), "ARCHIVED", now)
                insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ACTIVE", "ARCHIVED", cleanReason, cleanNote, now)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Restore a previously archived learner and append a history event without changing academic records. */
    fun restoreLearner(learnerId: String) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val learner = learnerRepository.getById(learnerId, activeSchoolId()) ?: error("Learner not found")
            require(learner.status == "ACTIVE" || learner.status == "ARCHIVED") {
                "Learner has an unsupported status; review the record before restoring."
            }
            if (learner.status == "ARCHIVED") {
                val now = System.currentTimeMillis()
                learnerRepository.setStatusInSchool(learnerId, activeSchoolId(), "ACTIVE", now)
                insertLearnerStatusEvent(db, learnerId, activeSchoolId(), "ARCHIVED", "ACTIVE", "Restored by teacher", null, now)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Latest recorded archive reason for the active school only. Lifecycle events carry
     * school_id so identical learner identities can be managed independently across schools.
     */
    // Pre-v10 audit rows are attributed to legacy-school by migration because their source school was not stored.
    fun latestArchiveReasons(): Map<String, String> {
        val schoolId = activeSchoolId()
        return database.readableDatabase.rawQuery(
            "SELECT h.learner_id, h.reason FROM learner_status_history h " +
                "WHERE h.to_status = 'ARCHIVED' AND h.school_id = ? " +
                "ORDER BY h.changed_at DESC, h.id DESC",
            arrayOf(schoolId)
        ).use { cursor ->
            buildMap {
                while (cursor.moveToNext()) {
                    val learnerId = cursor.getString(0)
                    if (!containsKey(learnerId)) put(learnerId, cursor.getString(1))
                }
            }
        }
    }

    private fun insertLearnerStatusEvent(
        db: android.database.sqlite.SQLiteDatabase,
        learnerId: String,
        schoolId: String,
        fromStatus: String,
        toStatus: String,
        reason: String,
        note: String?,
        changedAt: Long
    ) {
        val values = ContentValues().apply {
            put("id", UUID.randomUUID().toString())
            put("learner_id", learnerId)
            put("school_id", schoolId)
            put("from_status", fromStatus)
            put("to_status", toStatus)
            put("reason", reason)
            if (note == null) putNull("note") else put("note", note)
            put("changed_at", changedAt)
        }
        db.insertOrThrow("learner_status_history", null, values)
    }

    fun learnerHistory(learnerId: String, scale: GradingScale = GradingScale.default()): com.henrylumis.rvhgrader.core.history.LearnerAcademicHistory? {
        val schoolId = activeSchoolId()
        // Never resolve a globally shared learner identity before confirming school ownership.
        // This prevents a stale/deep-linked learner ID from exposing a name with an empty history.
        val enrollments = academicRepository.getEnrollmentsForLearner(learnerId, schoolId)
        if (enrollments.isEmpty()) return null
        val learner = learnerRepository.getById(learnerId, schoolId) ?: return null
        val histories = enrollments.map { enrollment ->
            val assessments = assessmentHistory(enrollment, scale).map { snapshot ->
                com.henrylumis.rvhgrader.core.history.AssessmentHistory(
                    assessmentId = snapshot.result.assessmentId,
                    termName = snapshot.assessmentLabel.substringBefore(" • "),
                    sectionName = snapshot.assessmentLabel.substringAfter(" • ", ""),
                    snapshot = snapshot
                )
            }
            com.henrylumis.rvhgrader.core.history.EnrollmentHistory(
                enrollmentId = enrollment.id,
                academicYear = enrollment.academicYear,
                schoolClassName = enrollment.schoolClass.displayName,
                stream = enrollment.stream,
                assessments = assessments
            )
        }
        return com.henrylumis.rvhgrader.core.history.AcademicHistoryCalculator.build(
            learnerId, learner.fullName, histories
        )
    }

    data class PolicyReviewEntry(val learnerId: String, val learnerName: String, val status: String, val detail: String)

    /** Read-only diagnostic view; it never changes marks or saved policy snapshots. */
    fun policyReview(year: Int, schoolClass: SchoolClass, term: Term, section: TermSection): List<PolicyReviewEntry> {
        data class Row(val entry: PolicyReviewEntry, val signature: List<Any>?)
        val rows = academicRepository.getEnrollments(year, schoolClass, activeSchoolId()).mapNotNull { enrollment ->
            val assessment = academicRepository.getAssessmentForSchool(enrollment.id, term, section, activeSchoolId()) ?: return@mapNotNull null
            val learner = learnerRepository.getById(enrollment.learnerId) ?: return@mapNotNull null
            val raw = assessment.policySnapshotJson
            val parsed = if (raw.isNullOrBlank()) null else runCatching { AssessmentPolicySnapshot.parse(raw) }.getOrNull()
            val status = when {
                raw.isNullOrBlank() -> "LEGACY / MISSING POLICY"
                parsed == null -> "INVALID POLICY"
                !parsed.hasCompleteScoringPolicy -> "LEGACY POLICY"
                else -> "SAVED POLICY"
            }
            val detail = when (status) {
                "LEGACY / MISSING POLICY" -> "No saved policy snapshot; class-wide comparisons may be blocked."
                "INVALID POLICY" -> "Saved policy could not be validated. Historical data was left unchanged."
                "LEGACY POLICY" -> "Older subject-only snapshot; scoring rules are not fully pinned."
                else -> "Subjects: ${parsed!!.subjects.size} • Pass threshold: ${parsed.classPassPercent}%"
            }
            val signature = parsed?.let { p -> listOf<Any>(p.schoolClass, p.subjects, p.aggregateBands, p.divisionBands, p.classPassPercent, p.hasCompleteScoringPolicy) }
            Row(PolicyReviewEntry(learner.id, learner.fullName, status, detail), signature)
        }
        val pinned = rows.filter { it.signature != null }
        val hasMixed = pinned.size != rows.size && pinned.isNotEmpty()
        val hasDifferentPinnedPolicies = pinned.map { it.signature }.distinct().size > 1
        return rows.map { row ->
            when {
                row.entry.status == "SAVED POLICY" && hasMixed -> row.entry.copy(status = "MIXED POLICY SET", detail = "This class selection mixes saved and legacy/missing policy records. Review all listed learners.")
                // Mark every policy group participating in a conflict, not just groups after the first.
                row.entry.status == "SAVED POLICY" && hasDifferentPinnedPolicies -> row.entry.copy(status = "CONFLICTING POLICY", detail = "This saved policy differs from another learner's policy; review before combining class results.")
                else -> row.entry
            }
        }
    }

    fun roster(year: Int, schoolClass: SchoolClass): List<RosterEntry> =
        academicRepository.getEnrollments(year, schoolClass, activeSchoolId()).mapNotNull { enrollment ->
            learnerRepository.getById(enrollment.learnerId, activeSchoolId())
                ?.takeIf { it.status == "ACTIVE" }
                ?.let { learner -> RosterEntry(learner, enrollment) }
        }

    override fun close() { academicRepository.close(); database.close() }
}
