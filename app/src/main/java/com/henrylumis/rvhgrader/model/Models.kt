package com.henrylumis.rvhgrader.model

import java.util.UUID

enum class SystemMode { LOWER, UPPER }

/**
 * Primary One through Seven. P1-P3 use the Lower Primary computation (7 subjects, ungraded);
 * P4-P7 use the Upper Primary computation (4 subjects, PLE-style aggregate + division).
 */
enum class SchoolClass(val displayName: String, val mode: SystemMode) {
    P1("Primary One", SystemMode.LOWER),
    P2("Primary Two", SystemMode.LOWER),
    P3("Primary Three", SystemMode.LOWER),
    P4("Primary Four", SystemMode.UPPER),
    P5("Primary Five", SystemMode.UPPER),
    P6("Primary Six", SystemMode.UPPER),
    P7("Primary Seven", SystemMode.UPPER)
}

enum class Term(val displayName: String) {
    TERM1("Term 1"),
    TERM2("Term 2"),
    TERM3("Term 3")
}

/**
 * Each term is further split into three assessment points. Not every school runs all three for
 * every term — that's fine, nothing here requires all three to exist. A section only shows up
 * anywhere in the app (standings, history, exports) once a record actually exists for it; an
 * un-done section is never displayed as an empty placeholder.
 */
enum class TermSection(val displayName: String) {
    BEG("Beginning of Term"),
    MID("Mid Term"),
    END("End of Term")
}

data class SubjectScore(
    val mark: Int,
    val agg: Int? = null
)

/**
 * A single learner's marksheet for one class + term + section. [id] is stable across edits so
 * History and Backup/Restore can track the same learner record reliably; [recordedAtMillis] is
 * what Student History sorts/orders by when showing a learner's progress over time.
 */
data class StudentRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val schoolClass: SchoolClass,
    val term: Term,
    val section: TermSection,
    val scores: Map<String, SubjectScore>,
    val total: Int,
    val aggSum: Int?,
    val division: String?,
    val graded: Boolean,
    val recordedAtMillis: Long = System.currentTimeMillis()
)
