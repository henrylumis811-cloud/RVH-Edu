import com.henrylumis.rvhgrader.core.importer.AcademicImportParser
import com.henrylumis.rvhgrader.model.SchoolClass

fun main() {
    val csv = "School Results\nNAME,English,Math,Math,Science\nAmina,80,70,99,60\nBrian,,bad,101,101"
    val parsed = AcademicImportParser.parseCsv(csv, SchoolClass.P4)
    check(parsed.rows.size == 2)
    check(parsed.rows[0].marks["eng"] == 80)
    check(parsed.rows[0].marks["mtc"] == 70)
    check(parsed.rows[0].marks["sci"] == 60)
    check(parsed.rows[1].marks["eng"] == null)
    check(parsed.warnings.any { it.contains("Duplicate subject header") })
    check(parsed.warnings.any { it.contains("invalid mtc") })
    check(parsed.warnings.any { it.contains("out-of-range sci") })
    println("IMPORT_PARSER_REGRESSION_OK")
}
