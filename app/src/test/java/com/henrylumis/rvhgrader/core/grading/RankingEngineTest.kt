package com.henrylumis.rvhgrader.core.grading

import com.henrylumis.rvhgrader.core.model.AcademicResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RankingEngineTest {
    private fun result(learnerId: String, assessmentId: String, total: Int, group: String? = null) = AcademicResult(
        learnerId = learnerId, assessmentId = assessmentId, total = total, maxTotal = 700,
        average = total / 7.0, aggregateSum = null, division = null, graded = true,
        complete = true, missingSubjects = emptyList(), assessmentGroupKey = group
    )

    @Test fun ranksDifferentPerLearnerAssessmentRowsWhenTheyShareOneClassAssessmentGroup() {
        val group = "school|2026|P1|TERM_1|BEGINNING"
        val ranked = RankingEngine.rankIncludingIncomplete(listOf(
            result("learner-a", "assessment-row-a", 600, group),
            result("learner-b", "assessment-row-b", 550, group),
            result("learner-c", "assessment-row-c", 500, group)
        ))
        assertEquals(listOf("learner-a", "learner-b", "learner-c"), ranked.map { it.result.learnerId })
        assertEquals(listOf(1, 2, 3), ranked.map { it.position })
    }

    @Test fun rejectsResultsFromDifferentAssessmentGroups() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            RankingEngine.rankIncludingIncomplete(listOf(
                result("learner-a", "assessment-row-a", 600, "school|2026|P1|TERM_1|BEGINNING"),
                result("learner-b", "assessment-row-b", 550, "school|2026|P1|TERM_1|MIDTERM")
            ))
        }
        assertEquals("Ranking results must belong to the same assessment group", error.message)
    }

    @Test fun retainsLegacySameAssessmentIdValidationWhenNoGroupKeyIsProvided() {
        assertThrows(IllegalArgumentException::class.java) {
            RankingEngine.rankIncludingIncomplete(listOf(
                result("learner-a", "assessment-a", 600), result("learner-b", "assessment-b", 550)
            ))
        }
    }
}
