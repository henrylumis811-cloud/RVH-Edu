import com.henrylumis.rvhgrader.core.analytics.AcademicAnalytics
import com.henrylumis.rvhgrader.core.analytics.ClassAnalyticsCalculator
import com.henrylumis.rvhgrader.core.model.AcademicResult
import com.henrylumis.rvhgrader.core.model.Mark
import com.henrylumis.rvhgrader.core.model.RankedResult

private fun expectRejected(block: () -> Unit) {
    var rejected = false
    try { block() } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected) { "Expected invalid pass threshold to be rejected" }
}

fun main() {
    val mark = Mark(assessmentId = "a1", subjectCode = "eng", mark = 45, maxMark = 100)
    val default = AcademicAnalytics.subjectAnalytics(listOf(listOf(mark))).single()
    check(default.passRatePercent == 0.0) { "45% must fail under the default 50% threshold" }

    val configured = AcademicAnalytics.subjectAnalytics(listOf(listOf(mark)), passPercent = 40.0).single()
    check(configured.passRatePercent == 100.0) { "45% must pass under a 40% threshold" }

    val result = AcademicResult("learner", "a1", 45, 100, 45.0, null, null, false, true, emptyList())
    val summary = ClassAnalyticsCalculator.summarize(
        listOf(RankedResult(result, 1)),
        mapOf("learner" to listOf(mark)),
        passPercent = 40.0
    )
    check(summary.passRatePercent == 100.0) { "Class pass rate must use the configured threshold" }
    check(summary.subjectAnalytics.single().passRatePercent == 100.0) {
        "Subject pass rate must use the same configured threshold as class pass rate"
    }

    val incompleteClass = ClassAnalyticsCalculator.summarize(
        results = listOf(
            RankedResult(result, 1),
            RankedResult(AcademicResult("learner-2", "a1", 0, 0, 0.0, null, null, false, false, listOf("eng")), 0)
        ),
        marksByLearner = mapOf("learner" to listOf(mark), "learner-2" to emptyList()),
        passPercent = 40.0
    )
    val english = incompleteClass.subjectAnalytics.single()
    check(english.learnerCount == 1) { "Only learners with an entered subject mark count as assessed" }
    check(english.missingCount == 1) { "Missing subject mark must be visible, not silently excluded" }
    check(english.coveragePercent == 50.0) { "Subject coverage must use the whole class as denominator" }
    check(english.passRatePercent == 100.0) { "Pass rate must remain based on assessed learners only" }

    val allSubjects = AcademicAnalytics.subjectAnalytics(
        marksByLearner = listOf(listOf(mark), emptyList()),
        expectedLearnerCount = 2,
        expectedSubjectCodes = listOf("eng", "mtc", "sci")
    ).associateBy { it.subjectCode }
    check(allSubjects.keys == setOf("eng", "mtc", "sci")) { "Catalogued subjects with no marks must still appear" }
    val maths = allSubjects.getValue("mtc")
    check(maths.learnerCount == 0 && maths.missingCount == 2 && maths.coveragePercent == 0.0) {
        "A wholly unassessed subject must show zero coverage and all learners missing"
    }
    check(maths.averagePercent == null && maths.passRatePercent == null) {
        "An unassessed subject must not display a misleading zero average/pass rate"
    }
    expectRejected {
        AcademicAnalytics.subjectAnalytics(listOf(listOf(mark, mark)), expectedLearnerCount = 1)
    }

    expectRejected { AcademicAnalytics.subjectAnalytics(listOf(listOf(mark)), Double.NaN) }
    expectRejected { AcademicAnalytics.subjectAnalytics(listOf(listOf(mark)), 100.01) }
    expectRejected { ClassAnalyticsCalculator.summarize(emptyList(), passPercent = -0.1) }
    println("SUBJECT_PASS_THRESHOLD_REGRESSION_OK")
}
