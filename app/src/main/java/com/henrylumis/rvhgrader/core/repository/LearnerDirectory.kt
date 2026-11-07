package com.henrylumis.rvhgrader.core.repository

import com.henrylumis.rvhgrader.core.model.Learner

/**
 * Simple learner-facing rules. Formal admission numbers are optional; name is the only required
 * teacher-entered identity field. Ambiguous matches are returned instead of guessed.
 */
class LearnerDirectory(private val repository: LearnerRepository) {
    fun create(
        name: String,
        ageYears: Int? = null,
        gender: String? = null,
        admissionNumber: String? = null
    ): Learner {
        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        require(cleanName.isNotEmpty()) { "Learner name is required" }
        require(ageYears == null || ageYears in 2..30) { "Learner age must be between 2 and 30" }

        val learner = Learner(
            fullName = cleanName,
            ageYears = ageYears,
            gender = gender?.trim()?.takeIf { it.isNotEmpty() },
            admissionNumber = admissionNumber?.trim()?.takeIf { it.isNotEmpty() }
        )
        repository.upsert(learner)
        return learner
    }

    fun findCandidates(name: String, ageYears: Int? = null): List<Learner> =
        repository.findByNameAndAge(name.trim().replace(Regex("\\s+"), " "), ageYears)
}
