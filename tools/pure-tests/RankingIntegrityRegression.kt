import com.henrylumis.rvhgrader.core.grading.RankingEngine
import com.henrylumis.rvhgrader.core.model.AcademicResult

fun main() {
    val completeA = AcademicResult("a", "x", 380, 400, 95.0, 4, "I", true, true, emptyList())
    val completeB = AcademicResult("b", "x", 360, 400, 90.0, 8, "I", true, true, emptyList())
    val incomplete = AcademicResult("c", "x", 120, 400, 30.0, null, null, true, false, listOf("sci"))
    val rows = RankingEngine.rankIncludingIncomplete(listOf(incomplete, completeB, completeA))
    check(rows.size == 3)
    check(rows[0].result.learnerId == "a" && rows[0].position == 1)
    check(rows[1].result.learnerId == "b" && rows[1].position == 2)
    check(rows[2].result.learnerId == "c" && rows[2].position == 0)
    check(RankingEngine.rank(listOf(incomplete, completeB, completeA)).size == 2)

    // Incomplete results are never assigned a position, even when a caller asks for every row.
    check(rows.last().position == 0)

    fun mustReject(label: String, candidate: List<AcademicResult>) {
        var rejected = false
        try { RankingEngine.rankIncludingIncomplete(candidate) } catch (_: IllegalArgumentException) { rejected = true }
        check(rejected) { "Ranking engine must reject $label" }
    }
    mustReject("duplicate learner results", listOf(completeA, completeA.copy(total = 350)))
    mustReject("mixed assessments", listOf(completeA, completeB.copy(assessmentId = "other")))
    mustReject("invalid total", listOf(completeA.copy(total = 401)))
    mustReject("zero maximum total", listOf(completeA.copy(maxTotal = 0)))
    mustReject("invalid average", listOf(completeA.copy(average = Double.NaN)))
    mustReject("inconsistent completeness", listOf(incomplete.copy(missingSubjects = emptyList())))
    println("RANKING_INTEGRITY_REGRESSION_OK")
}
