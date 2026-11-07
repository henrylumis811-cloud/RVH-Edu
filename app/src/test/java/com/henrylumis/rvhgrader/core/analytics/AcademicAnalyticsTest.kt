package com.henrylumis.rvhgrader.core.analytics

import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.model.GradingScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicAnalyticsTest {
    @Test
    fun subjectAnalyticsCountsUgandanGradeBandsAndCalculatesAmgp() {
        val scores = listOf(95, 85, 75, 65, 55, 42, 20)
        val marks = scores.map { score -> Mark(assessmentId = "assessment-1", subjectCode = "MATH", mark = score) }
        val subject = AcademicAnalytics.subjectAnalytics(
            marksByLearner = marks.map { listOf(it) }, expectedLearnerCount = 8,
            expectedSubjectCodes = listOf("MATH"), gradingScale = GradingScale.default()
        ).single()
        assertEquals(2, subject.distinctionCount)
        assertEquals(3, subject.creditCount)
        assertEquals(1, subject.passCount)
        assertEquals(1, subject.failureCount)
        assertEquals(7, subject.learnerCount)
        assertEquals(1, subject.missingCount)
        assertEquals(2.14, subject.amgp!!, 0.001)
    }

    @Test
    fun subjectWithNoMarksHasNullAmgpAndZeroCoverage() {
        val subject = AcademicAnalytics.subjectAnalytics(
            marksByLearner = emptyList(), expectedLearnerCount = 12, expectedSubjectCodes = listOf("ENG")
        ).single()
        assertNull(subject.amgp)
        assertEquals(0, subject.learnerCount)
        assertEquals(12, subject.missingCount)
        assertEquals(0.0, subject.coveragePercent, 0.001)
    }

    @Test
    fun analyticsNormalizesMarksWhenMaximumIsNotOneHundred() {
        val marks = listOf(
            Mark(assessmentId = "a", subjectCode = "SCI", mark = 18, maxMark = 20),
            Mark(assessmentId = "a", subjectCode = "SCI", mark = 8, maxMark = 10)
        )
        val subject = AcademicAnalytics.subjectAnalytics(
            marksByLearner = marks.map { listOf(it) },
            expectedSubjectCodes = listOf("SCI")
        ).single()
        assertEquals(85.0, subject.averagePercent!!, 0.001)
        assertEquals(90.0, subject.highestPercent!!, 0.001)
        assertEquals(80.0, subject.lowestPercent!!, 0.001)
        assertEquals(100.0, subject.coveragePercent, 0.001)
    }

    @Test
    fun analyticsIncludesUnassessedCatalogSubjectsAndKeepsCoverageBounded() {
        val subject = AcademicAnalytics.subjectAnalytics(
            marksByLearner = listOf(emptyList(), emptyList()),
            expectedLearnerCount = 2,
            expectedSubjectCodes = listOf("ENG", "MATH")
        )
        assertEquals(listOf("ENG", "MATH"), subject.map { it.subjectCode })
        assertTrue(subject.all { it.amgp == null && it.averagePercent == null })
        assertTrue(subject.all { it.coveragePercent == 0.0 && it.missingCount == 2 })
    }

    @Test
    fun subjectPassRateUsesOnlyAssessedLearnersAndConfiguredThreshold() {
        val marks = listOf(
            Mark(assessmentId = "a", subjectCode = "SCI", mark = 80),
            Mark(assessmentId = "a", subjectCode = "SCI", mark = 40)
        )
        val subject = AcademicAnalytics.subjectAnalytics(
            marksByLearner = listOf(listOf(marks[0]), listOf(marks[1]), emptyList()),
            passPercent = 60.0,
            expectedLearnerCount = 3,
            expectedSubjectCodes = listOf("SCI")
        ).single()
        assertEquals(50.0, subject.passRatePercent!!, 0.001)
        assertEquals(66.67, subject.coveragePercent, 0.01)
        assertEquals(2, subject.learnerCount)
        assertEquals(1, subject.missingCount)
    }

    @Test
    fun analyticsRejectsDuplicateSubjectMarksForOneLearner() {
        val duplicateMarks = listOf(
            Mark(assessmentId = "a", subjectCode = "ENG", mark = 60),
            Mark(assessmentId = "a", subjectCode = "ENG", mark = 70)
        )
        val error = runCatching { AcademicAnalytics.subjectAnalytics(marksByLearner = listOf(duplicateMarks)) }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class.java, error?.javaClass)
    }
}
