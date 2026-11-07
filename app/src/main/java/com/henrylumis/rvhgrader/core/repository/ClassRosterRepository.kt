package com.henrylumis.rvhgrader.core.repository

import android.content.Context
import com.henrylumis.rvhgrader.core.database.RvhEduDatabase
import com.henrylumis.rvhgrader.core.model.Enrollment
import com.henrylumis.rvhgrader.core.model.Learner
import com.henrylumis.rvhgrader.data.SchoolManagementRepository
import com.henrylumis.rvhgrader.model.SchoolClass

/** Read model for teacher-facing class rosters. */
class ClassRosterRepository(private val context: Context) : AutoCloseable {
    private val db = RvhEduDatabase(context)
    private val learners = LearnerRepository(db)
    private val academics = AcademicRepository(db)

    data class RosterEntry(val learner: Learner, val enrollment: Enrollment)

    fun get(year: Int, schoolClass: SchoolClass): List<RosterEntry> =
        academics.getEnrollments(year, schoolClass, SchoolManagementRepository.activeId(context)).mapNotNull { enrollment ->
            learners.getById(enrollment.learnerId, SchoolManagementRepository.activeId(context))?.let { RosterEntry(it, enrollment) }
        }.sortedBy { it.learner.fullName.lowercase() }

    override fun close() { academics.close(); db.close() }
}
