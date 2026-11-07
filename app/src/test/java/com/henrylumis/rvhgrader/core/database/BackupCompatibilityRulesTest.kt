package com.henrylumis.rvhgrader.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class BackupCompatibilityRulesTest {
    @Test fun acceptsCurrentAndLegacyBackupEnvelopes() {
        BackupCompatibilityRules.requireSupportedBackupVersion(1)
        BackupCompatibilityRules.requireSupportedBackupVersion(2)
    }

    @Test fun rejectsUnknownBackupVersions() {
        assertRejected("Unsupported RVH Edu backup version.") { BackupCompatibilityRules.requireSupportedBackupVersion(0) }
        assertRejected("Unsupported RVH Edu backup version.") { BackupCompatibilityRules.requireSupportedBackupVersion(3) }
        assertRejected("Unsupported RVH Edu backup version.") { BackupCompatibilityRules.requireSupportedBackupVersion(-1) }
    }

    @Test fun acceptsOldestAndCurrentSupportedSchema() {
        BackupCompatibilityRules.requireSupportedSchemaVersion(4, 10)
        BackupCompatibilityRules.requireSupportedSchemaVersion(10, 10)
    }

    @Test fun rejectsSchemasOutsideSupportedRange() {
        assertRejected("Unsupported database schema in this backup. Supported schemas: 4 and 10.") { BackupCompatibilityRules.requireSupportedSchemaVersion(3, 10) }
        assertRejected("Unsupported database schema in this backup. Supported schemas: 4 and 10.") { BackupCompatibilityRules.requireSupportedSchemaVersion(11, 10) }
        assertRejected("Unsupported database schema in this backup. Supported schemas: 4 and 10.") { BackupCompatibilityRules.requireSupportedSchemaVersion(0, 10) }
    }

    private fun assertRejected(message: String, block: () -> Unit) {
        try { block(); fail("Expected validation to reject the value") }
        catch (error: IllegalArgumentException) { assertEquals(message, error.message) }
    }
}
