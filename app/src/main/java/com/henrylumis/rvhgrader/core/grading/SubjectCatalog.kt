package com.henrylumis.rvhgrader.core.grading

import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.SystemMode

/** Single source of truth for the default subjects expected by each class level. */
object SubjectCatalog {
    data class Subject(val code: String, val shortName: String, val displayName: String)

    @Volatile private var lower = listOf(
        Subject("eng", "ENG", "English"), Subject("mtc", "MTC", "Mathematics"),
        Subject("liti", "LIT I", "Literacy I"), Subject("read", "READ", "Reading"),
        Subject("writ", "WRIT", "Writing"), Subject("re", "R.E", "Religious Education"),
        Subject("lug", "LUG", "Luganda")
    )
    @Volatile private var upper = listOf(
        Subject("eng", "ENG", "English"), Subject("mtc", "MTC", "Mathematics"),
        Subject("sci", "SCI", "Science"), Subject("sst", "S.S.T", "Social Studies")
    )

    fun forClass(schoolClass: SchoolClass): List<Subject> = forMode(schoolClass.mode)

    @Synchronized
    fun configure(lowerSubjects: List<Subject>, upperSubjects: List<Subject>) {
        validateGroup(lowerSubjects, "Lower-primary")?.let { require(false) { it } }
        validateGroup(upperSubjects, "Upper-primary")?.let { require(false) { it } }
        lower = lowerSubjects.toList()
        upper = upperSubjects.toList()
    }

    fun defaultSubjects(mode: SystemMode): List<Subject> =
        if (mode == SystemMode.LOWER) defaultLower else defaultUpper

    fun forMode(mode: SystemMode): List<Subject> =
        if (mode == SystemMode.LOWER) lower.toList() else upper.toList()

    private val defaultLower get() = listOf(
        Subject("eng", "ENG", "English"), Subject("mtc", "MTC", "Mathematics"),
        Subject("liti", "LIT I", "Literacy I"), Subject("read", "READ", "Reading"),
        Subject("writ", "WRIT", "Writing"), Subject("re", "R.E", "Religious Education"),
        Subject("lug", "LUG", "Luganda")
    )
    private val defaultUpper get() = listOf(
        Subject("eng", "ENG", "English"), Subject("mtc", "MTC", "Mathematics"),
        Subject("sci", "SCI", "Science"), Subject("sst", "S.S.T", "Social Studies")
    )

    /** All built-in subjects, deduplicated by stable code. */
    fun all(): List<Subject> = (lower + upper).distinctBy { it.code }

    /** Validate both school subject groups before they are persisted. */
    fun validate(lowerSubjects: List<Subject>, upperSubjects: List<Subject>): String? =
        validateGroup(lowerSubjects, "Lower-primary") ?: validateGroup(upperSubjects, "Upper-primary")

    /** Validate catalog integrity before it is consumed by grading or reporting code. */
    fun validationError(): String? = validate(lower, upper)

    private fun validateGroup(subjects: List<Subject>, groupName: String): String? {
        if (subjects.isEmpty()) return "$groupName subject list cannot be empty."
        if (subjects.any { it.code.isBlank() || it.shortName.isBlank() || it.displayName.isBlank() }) {
            return "$groupName subjects must have a code, short name and display name."
        }
        if (subjects.any { !it.code.matches(Regex("[a-z][a-z0-9_]{1,19}")) }) {
            return "$groupName subject codes must start with a lowercase letter and contain only lowercase letters, digits or underscores."
        }
        if (subjects.map { it.code }.distinct().size != subjects.size) return "$groupName subject codes must be unique."
        if (subjects.map { it.displayName.trim().lowercase() }.distinct().size != subjects.size) return "$groupName subject names must be unique."
        return null
    }

    fun contains(schoolClass: SchoolClass, code: String): Boolean =
        forClass(schoolClass).any { it.code == code }
}
