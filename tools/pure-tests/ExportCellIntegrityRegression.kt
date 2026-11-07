import com.henrylumis.rvhgrader.export.CsvCellEncoder

fun main() {
    check(CsvCellEncoder.encode("Alice") == "Alice")
    check(CsvCellEncoder.encode("A, B") == "\"A, B\"")
    check(CsvCellEncoder.encode("She said \"hi\"") == "\"She said \"\"hi\"\"\"")
    check(CsvCellEncoder.encode("=1+1") == "'=1+1")
    check(CsvCellEncoder.encode("  @SUM(A1:A2)") == "'  @SUM(A1:A2)")
    check(CsvCellEncoder.encode("-cmd") == "'-cmd")
    check(CsvCellEncoder.encode("-1+1") == "'-1+1")
    check(CsvCellEncoder.encode("-12") == "-12")
    check(CsvCellEncoder.encode("-") == "-")
    check(CsvCellEncoder.encode("line1\nline2") == "\"line1\nline2\"")
    println("CSV_EXPORT_CELL_INTEGRITY_OK")
}
