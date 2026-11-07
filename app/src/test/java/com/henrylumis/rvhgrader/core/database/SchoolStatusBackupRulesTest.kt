package com.henrylumis.rvhgrader.core.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolStatusBackupRulesTest {
    @Test
    fun statusForOneSchoolDoesNotSatisfyAnotherSchoolsEnrollment() {
        val statuses = setOf(SchoolStatusBackupRules.key("school-a", "learner-1"))

        assertTrue(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school-a", "learner-1"))
        assertFalse(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school-b", "learner-1"))
    }

    @Test
    fun learnerCanHaveIndependentStatusesInMultipleSchools() {
        val statuses = setOf(
            SchoolStatusBackupRules.key("school-a", "learner-1"),
            SchoolStatusBackupRules.key("school-b", "learner-1")
        )

        assertTrue(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school-a", "learner-1"))
        assertTrue(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school-b", "learner-1"))
        assertTrue(SchoolStatusBackupRules.hasAnyStatusForLearner(statuses, "learner-1"))
    }

    @Test
    fun statusForDifferentLearnerCannotSatisfyEnrollment() {
        val statuses = setOf(SchoolStatusBackupRules.key("school-a", "learner-2"))

        assertFalse(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school-a", "learner-1"))
        assertFalse(SchoolStatusBackupRules.hasAnyStatusForLearner(statuses, "learner-1"))
    }

    @Test
    fun schoolAndLearnerIdsAreComparedAsPairsNotDelimiterConcatenations() {
        val statuses = setOf(SchoolStatusBackupRules.key("school\u001fpart", "learner"))

        assertTrue(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school\u001fpart", "learner"))
        assertFalse(SchoolStatusBackupRules.hasStatusForEnrollment(statuses, "school", "part\u001flearner"))
    }
}
