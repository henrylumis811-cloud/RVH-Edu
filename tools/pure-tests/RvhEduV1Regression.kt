import com.henrylumis.rvhgrader.core.grading.AcademicEngine
import com.henrylumis.rvhgrader.core.grading.AssessmentWorkflow
import com.henrylumis.rvhgrader.core.grading.RankingEngine
import com.henrylumis.rvhgrader.core.grading.SubjectCatalog
import com.henrylumis.rvhgrader.core.model.AcademicAssessment
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.reports.CertificateBuilder
import com.henrylumis.rvhgrader.core.reports.ReportCardBuilder
import com.henrylumis.rvhgrader.model.GradingScale
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.SystemMode
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import com.henrylumis.rvhgrader.core.model.Learner

private fun checkThat(condition: Boolean, message: String) {
    check(condition) { "FAIL: $message" }
}

fun main() {
    val scale = GradingScale.default()
    val lowerSubjects = SubjectCatalog.forClass(SchoolClass.P3).map { it.code }
    val upperSubjects = SubjectCatalog.forClass(SchoolClass.P4).map { it.code }

    val incomplete = AssessmentWorkflow.toMarks(
        "a1", listOf(
            AssessmentWorkflow.MarkInput(lowerSubjects[0], 80),
            AssessmentWorkflow.MarkInput(lowerSubjects[1], null)
        )
    )
    val lowerResult = AcademicEngine.calculate("l1", "a1", SystemMode.LOWER, lowerSubjects, incomplete, scale)
    checkThat(!lowerResult.complete, "missing mark must make result incomplete")
    checkThat(lowerResult.missingSubjects.contains(lowerSubjects[1]), "missing subject must be explicit")
    checkThat(!lowerResult.graded, "lower primary must not be graded with aggregate/division")

    val completeMarks = upperSubjects.mapIndexed { index, code ->
        Mark(assessmentId = "a2", subjectCode = code, mark = 90 - index * 10)
    }
    val upperResult = AcademicEngine.calculate("l2", "a2", SystemMode.UPPER, upperSubjects, completeMarks, scale)
    checkThat(upperResult.complete, "complete upper result must be complete")
    checkThat(upperResult.graded, "complete upper result must be graded")
    checkThat(upperResult.aggregateSum == 10, "90/80/70/60 should aggregate to 1+2+3+4")
    checkThat(upperResult.division == "I", "aggregate 10 should be Division I")

    var rejected = false
    try { AssessmentWorkflow.toMarks("a3", listOf(AssessmentWorkflow.MarkInput("eng", 101))) } catch (_: IllegalArgumentException) { rejected = true }
    checkThat(rejected, "mark above maximum must be rejected")

    rejected = false
    try { AssessmentWorkflow.toMarks("a4", listOf(AssessmentWorkflow.MarkInput("eng", 20), AssessmentWorkflow.MarkInput("eng", 30))) } catch (_: IllegalArgumentException) { rejected = true }
    checkThat(rejected, "duplicate subject input must be rejected")

    fun engineMustReject(label: String, candidate: List<Mark>) {
        var didReject = false
        try {
            AcademicEngine.calculate("l2", "a2", SystemMode.UPPER, upperSubjects, candidate, scale)
        } catch (_: IllegalArgumentException) {
            didReject = true
        }
        checkThat(didReject, "academic engine must reject $label")
    }
    engineMustReject("duplicate subject marks", completeMarks + completeMarks.first())
    engineMustReject("marks from another assessment", completeMarks.dropLast(1) + completeMarks.last().copy(assessmentId = "other"))
    engineMustReject("unexpected subject", completeMarks.dropLast(1) + completeMarks.last().copy(subjectCode = "not-assigned"))
    engineMustReject("negative marks", completeMarks.dropLast(1) + completeMarks.last().copy(mark = -1))
    engineMustReject("marks above maximum", completeMarks.dropLast(1) + completeMarks.last().copy(mark = 101))
    engineMustReject("zero maximum mark", completeMarks.dropLast(1) + completeMarks.last().copy(maxMark = 0))

    val r1 = AcademicResult("a", "x", 360, 400, 90.0, 10, "I", true, true, emptyList())
    val r2 = AcademicResult("b", "x", 360, 400, 90.0, 10, "I", true, true, emptyList())
    val r3 = AcademicResult("c", "x", 200, 400, 50.0, 20, "II", true, true, emptyList())
    val incompleteResult = r3.copy(learnerId = "d", complete = false, missingSubjects = listOf("eng"))
    val ranked = RankingEngine.rank(listOf(r3, incompleteResult, r1, r2))
    checkThat(ranked.size == 3, "incomplete learner must not be ranked")
    checkThat(ranked[0].position == 1 && ranked[1].position == 1 && ranked[2].position == 3, "ranking ties must use competition positions")

    val learner = Learner(id = "l2", fullName = "Test Learner")
    val assessment = AcademicAssessment(id = "a2", enrollmentId = "enroll", term = Term.TERM1, section = TermSection.END)
    val report = ReportCardBuilder.build("Test School", learner, SchoolClass.P4, 2026, assessment, completeMarks, upperResult)
    checkThat(report.subjects.size == upperSubjects.size, "report card must contain every expected subject")

    var reportRejected = false
    try {
        ReportCardBuilder.build("Test School", learner, SchoolClass.P4, 2026, assessment,
            completeMarks + completeMarks.first(), upperResult)
    } catch (_: IllegalArgumentException) { reportRejected = true }
    checkThat(reportRejected, "report builder must reject duplicate subject marks")

    reportRejected = false
    try {
        ReportCardBuilder.build("Test School", learner, SchoolClass.P4, 2026, assessment,
            completeMarks, upperResult.copy(total = upperResult.total + 1))
    } catch (_: IllegalArgumentException) { reportRejected = true }
    checkThat(reportRejected, "report builder must reject result totals inconsistent with marks")

    reportRejected = false
    try {
        ReportCardBuilder.build("Test School", learner.copy(id = "other"), SchoolClass.P4, 2026, assessment,
            completeMarks, upperResult)
    } catch (_: IllegalArgumentException) { reportRejected = true }
    checkThat(reportRejected, "report builder must reject a result belonging to another learner")

    checkThat(CertificateBuilder.build("Test School", learner, SchoolClass.P4, 2026, upperResult) != null, "complete result must be certificate-eligible")
    checkThat(CertificateBuilder.build("Test School", learner, SchoolClass.P4, 2026, incompleteResult) == null, "incomplete result must never earn a certificate")

    println("RVH_EDU_V1_REGRESSION_OK")
}
