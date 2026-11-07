package com.henrylumis.rvhgrader.core.database

/** Pure compatibility gates shared by backup preview and restore preflight. */
internal object BackupCompatibilityRules {
    fun requireSupportedBackupVersion(version: Int) {
        require(version == 1 || version == 2) { "Unsupported RVH Edu backup version." }
    }

    fun requireSupportedSchemaVersion(version: Int, currentSchemaVersion: Int) {
        require(version in 4..currentSchemaVersion) {
            "Unsupported database schema in this backup. Supported schemas: 4 and $currentSchemaVersion."
        }
    }
}
