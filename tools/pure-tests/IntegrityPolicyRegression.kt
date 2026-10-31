/** Static policy regression for V1 data-integrity rules. */
fun main() {
    val migration = java.io.File("app/src/main/java/com/henrylumis/rvhgrader/core/migration/LegacyGradebookMigration.kt").readText()
    val assessmentMigration = migration.substringAfter("private fun upsertAssessment").substringBefore("private fun addIdentityReview")
    check("CONFLICT_REPLACE" !in assessmentMigration) { "Assessment migration must never use destructive REPLACE" }
    check("insertOrThrow" in assessmentMigration) { "Assessment migration must insert safely" }

    val repository = java.io.File("app/src/main/java/com/henrylumis/rvhgrader/core/repository/AcademicRepository.kt").readText()
    check("database.delete(\"mark\"" in repository) { "Expected transactional mark replacement" }
    check("beginTransaction()" in repository) { "Expected transaction protection" }

    val backup = java.io.File("app/src/main/java/com/henrylumis/rvhgrader/core/database/AcademicDatabaseBackupManager.kt").readText()
    check("AcademicDatabaseIntegrity.requireHealthy(db)" in backup) { "Backup/restore must run integrity audit" }
    check("validateBackupRows(db, tableObject)" in backup) { "Backup rows must be validated before replacing current data" }
    check("root.optInt(\"schemaVersion\") == SCHEMA_VERSION" in backup) { "Restore must reject unsupported schema versions rather than pretending to migrate them" }
    check("MAX_BACKUP_ROWS" in backup) { "Restore must impose a safety limit on row counts" }
    check("finally {\n            helper.close()" in backup) { "Backup database helper must close even when export fails" }
    check("__rvhEduBlobBase64" in backup) { "Binary backup values need lossless encoding" }

    val ranking = java.io.File("app/src/main/java/com/henrylumis/rvhgrader/core/grading/RankingEngine.kt").readText()
    check("rankIncludingIncomplete" in ranking) { "Incomplete learners need an explicit unranked path" }
    println("INTEGRITY_POLICY_REGRESSION_OK")
}
