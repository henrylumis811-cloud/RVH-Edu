package com.henrylumis.rvhgrader.core.assessment

import com.henrylumis.rvhgrader.core.model.Enrollment
import com.henrylumis.rvhgrader.core.model.Learner

/** Learner identity plus the enrollment that places them in a class for an academic year. */
data class RosterEntry(
    val learner: Learner,
    val enrollment: Enrollment
)
