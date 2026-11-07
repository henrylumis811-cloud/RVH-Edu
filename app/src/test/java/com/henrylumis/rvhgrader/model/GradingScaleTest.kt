package com.henrylumis.rvhgrader.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradingScaleTest {
    @Test
    fun defaultScaleCoversEveryScoreFromZeroToOneHundred() {
        val scale = GradingScale.default()
        assertNull(scale.validationError())
        assertEquals(9, scale.aggregateFor(0))
        assertEquals(8, scale.aggregateFor(35))
        assertEquals(1, scale.aggregateFor(100))
    }

    @Test
    fun validationRejectsDuplicateMarkThresholds() {
        val scale = GradingScale(
            aggregateBands = listOf(AggregateBand(0, 9), AggregateBand(0, 8)),
            divisionBands = listOf(DivisionBand(12, "I"))
        )
        assertEquals("Each mark threshold must be unique.", scale.validationError())
    }

    @Test
    fun validationRejectsDivisionLabelsThatDifferOnlyByCase() {
        val scale = GradingScale(
            aggregateBands = listOf(AggregateBand(0, 9)),
            divisionBands = listOf(DivisionBand(12, "Division I"), DivisionBand(24, " division i "))
        )
        assertEquals("Division labels must be unique.", scale.validationError())
    }
}
