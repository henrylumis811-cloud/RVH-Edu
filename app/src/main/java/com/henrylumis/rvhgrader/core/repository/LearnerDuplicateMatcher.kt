package com.henrylumis.rvhgrader.core.repository

import com.henrylumis.rvhgrader.core.model.Learner
import java.text.Normalizer
import java.util.Locale

/** A review suggestion only. This class never merges, edits, or archives learner records. */
data class PossibleLearnerDuplicate(
    val learner: Learner,
    val reasons: List<String>,
    val confidence: Confidence
) {
    enum class Confidence { HIGH, REVIEW }
}

object LearnerDuplicateMatcher {
    fun findCandidates(proposed: Learner, existingLearners: Collection<Learner>): List<PossibleLearnerDuplicate> {
        val proposedAdmission = normalizeAdmission(proposed.admissionNumber)
        val proposedName = normalizeName(proposed.fullName)
        val proposedDob = normalizeDate(proposed.dateOfBirth)
        return existingLearners.asSequence()
            .filter { it.id != proposed.id }
            .mapNotNull { existing ->
                val reasons = mutableListOf<String>()
                val admission = proposedAdmission != null && proposedAdmission == normalizeAdmission(existing.admissionNumber)
                val sameDob = proposedDob != null && proposedDob == normalizeDate(existing.dateOfBirth)
                val sameName = proposedName.isNotEmpty() && proposedName == normalizeName(existing.fullName)
                val sameAge = proposed.ageYears != null && existing.ageYears != null && proposed.ageYears == existing.ageYears
                val sameGender = !proposed.gender.isNullOrBlank() && !existing.gender.isNullOrBlank() &&
                    normalizeName(proposed.gender) == normalizeName(existing.gender)

                if (admission) reasons += "Same normalized admission number"
                if (sameDob) reasons += "Same date of birth"
                if (sameName) reasons += "Same normalized full name"
                if (sameName && sameAge) reasons += "Same recorded age"
                if (sameName && sameGender) reasons += "Same recorded gender"

                // An admission number or DOB match is a strong warning. A name match is only
                // a review suggestion: siblings and unrelated learners can share names.
                if (admission || sameDob || sameName) {
                    PossibleLearnerDuplicate(
                        learner = existing,
                        reasons = reasons.distinct(),
                        confidence = if (admission || sameDob) PossibleLearnerDuplicate.Confidence.HIGH
                            else PossibleLearnerDuplicate.Confidence.REVIEW
                    )
                } else null
            }
            .sortedWith(compareBy<PossibleLearnerDuplicate> { it.confidence.ordinal }
                .thenBy { normalizeName(it.learner.fullName) }
                .thenBy { it.learner.id })
            .toList()
    }

    fun normalizeName(value: String?): String = Normalizer.normalize(value.orEmpty(), Normalizer.Form.NFKC)
        .trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

    fun normalizeAdmission(value: String?): String? = value?.let { normalizeName(it).uppercase(Locale.ROOT) }
        ?.takeIf { it.isNotBlank() }

    private fun normalizeDate(value: String?): String? = value?.trim()?.takeIf { it.isNotEmpty() }
}
