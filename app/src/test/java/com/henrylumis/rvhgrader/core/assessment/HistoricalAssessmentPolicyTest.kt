package com.henrylumis.rvhgrader.core.assessment

import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.model.AggregateBand
import com.henrylumis.rvhgrader.model.DivisionBand
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HistoricalAssessmentPolicyTest {
    @Test
    fun completeSavedPolicyOverridesLaterCurrentGradingScaleForAnalytics() {
        val savedScale = GradingScale(
            aggregateBands = listOf(AggregateBand(0, 7)),
            divisionBands = listOf(DivisionBand(99, "Saved"))
        )
        val currentScale = GradingScale(
            aggregateBands = listOf(AggregateBand(0, 9)),
            divisionBands = listOf(DivisionBand(99, "Current"))
        )
        val snapshot = AssessmentPolicySnapshot(
            schemaVersion = AssessmentPolicySnapshot.CURRENT_SCHEMA_VERSION,
            schoolClass = SchoolClass.P1.name,
            capturedAtMillis = 1L,
            subjects = emptyList(),
            aggregateBands = savedScale.aggregateBands,
            divisionBands = savedScale.divisionBands
        )

        val selected = analyticsGradingScale(snapshot, currentScale)
        assertEquals(savedScale, selected)
        assertNotEquals(currentScale, selected)
    }

    @Test
    fun legacySubjectOnlyPolicyUsesCurrentScaleForAnalytics() {
        val currentScale = GradingScale.default()
        val legacy = AssessmentPolicySnapshot(
            schemaVersion = 1,
            schoolClass = SchoolClass.P1.name,
            capturedAtMillis = 1L,
            subjects = emptyList()
        )
        assertEquals(currentScale, analyticsGradingScale(legacy, currentScale))
    }

    @Test
    fun absentAssessmentIsExcludedButActualLegacyAssessmentRemainsAsNullPolicy() {
        val pinnedJson = "{\"schemaVersion\":2}"
        val persisted = AcademicAssessment(
            enrollmentId = "enrolled-1", term = Term.TERM1,
            section = TermSection.BEG, policySnapshotJson = pinnedJson
        )
        val legacy = AcademicAssessment(
            enrollmentId = "enrolled-2", term = Term.TERM1,
            section = TermSection.BEG, policySnapshotJson = null
        )

        assertEquals(listOf(pinnedJson), policySnapshotsForExistingAssessments(listOf(persisted, null)))
        assertEquals(listOf(pinnedJson, null), policySnapshotsForExistingAssessments(listOf(persisted, legacy, null)))
    }
}
