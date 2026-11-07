import com.henrylumis.rvhgrader.core.importer.AcademicImportParser
import com.henrylumis.rvhgrader.core.importer.ImportReviewBuilder
import com.henrylumis.rvhgrader.core.model.Learner
import com.henrylumis.rvhgrader.model.SchoolClass

fun main() {
    val learners = listOf(
        Learner(fullName = "Amina Namukasa"),
        Learner(fullName = "Amina Namukasa"),
        Learner(fullName = "Brian Okello")
    )
    val sheet = AcademicImportParser.ParsedSheet(
        listOf(
            AcademicImportParser.Row("Amina Namukasa", mapOf("eng" to 80)),
            AcademicImportParser.Row("Brian OkelIo", mapOf("eng" to 70)),
            AcademicImportParser.Row("New Learner", mapOf("eng" to 60))
        ), emptyList()
    )
    val rows = ImportReviewBuilder.build(sheet, learners)
    check(rows[0].candidates.size == 2)
    check(rows[0].warning!!.contains("Multiple learners"))
    check(rows[1].candidates.isEmpty())
    check(rows[1].warning!!.contains("Similar existing name"))
    check(rows[2].warning == null)
    println("IMPORT_REVIEW_REGRESSION_OK")
}
