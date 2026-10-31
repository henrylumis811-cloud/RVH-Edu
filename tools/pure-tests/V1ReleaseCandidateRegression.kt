import com.henrylumis.rvhgrader.core.analytics.AcademicAnalytics
import com.henrylumis.rvhgrader.core.analytics.AssessmentSnapshot
import com.henrylumis.rvhgrader.core.grading.AcademicEngine
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.model.Learner
import com.henrylumis.rvhgrader.core.reports.CertificateBuilder
import com.henrylumis.rvhgrader.core.reports.ReportCardBuilder
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.SystemMode
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection

private fun ok(condition: Boolean, message: String) { check(condition) { "FAIL: $message" } }

fun main() {
    val scale = GradingScale.default()
    val subjects = SubjectCatalog.forClass(SchoolClass.P4).map { it.code }
    val marks = subjects.mapIndexed { i, code -> Mark(id = "m$i", assessmentId = "a", subjectCode = code, mark = 90 - i * 10) }
    val result = AcademicEngine.calculate("learner", "a", SystemMode.UPPER, subjects, marks, scale)
    ok(result.complete, "complete result")
    ok(result.aggregateSum == 10 && result.division == "I", "grading parity")

    val learner = Learner(fullName = "Release Candidate")
    val assessment = AcademicAssessment(enrollmentId = "e", term = Term.TERM1, section = TermSection.END)
    val report = ReportCardBuilder.build("Test School", learner, SchoolClass.P4, 2026, assessment, marks, result)
    ok(report.subjects.size == subjects.size, "report contains all subjects")
    ok(CertificateBuilder.build("Test School", learner, SchoolClass.P4, 2026, result) != null, "complete certificate eligibility")

    fun snap(year: Int, term: Int, section: Int, avg: Double): AssessmentSnapshot =
        AssessmentSnapshot("learner", year, term, section, "$year/$term/$section", AcademicResult("learner", "$year-$term-$section", (avg * 4).toInt(), 400, avg, null, null, false, true, emptyList()), emptyList())

    val history = listOf(snap(2026, 0, 2, 45.0), snap(2026, 1, 0, 90.0))
    val beforeTerm2 = history.filter { it.academicYear < 2026 || (it.academicYear == 2026 && (it.termOrder < 1 || (it.termOrder == 1 && it.sectionOrder <= 0))) }
    val progress = AcademicAnalytics.learnerProgress(beforeTerm2).single()
    ok(progress.latestAverage == 90.0, "historical cutoff keeps selected assessment as latest")
    ok(progress.previousAverage == 45.0 && progress.changePoints == 45.0, "historical trend uses only prior data")

    println("RVH_EDU_V1_RELEASE_CANDIDATE_OK")
}
